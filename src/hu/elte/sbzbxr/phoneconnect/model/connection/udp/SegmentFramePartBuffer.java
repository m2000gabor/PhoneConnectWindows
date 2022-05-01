package hu.elte.sbzbxr.phoneconnect.model.connection.udp;

import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.SegmentFrame;
import hu.elte.sbzbxr.phoneconnect.model.connection.common.items.UdpSegmentFramePart;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SegmentFramePartBuffer {
    public static final int WAIT_TILL_FINISHED = 10; //Number of turns to wait until the id considered to be finished
    private final Map<Long,List<UdpSegmentFramePart>> partMap = new HashMap<>();
    private final Map<Long, AtomicInteger> counterMap = new HashMap<>();
    private final Set<Long> finished = new HashSet<>(4);

    public void add(UdpSegmentFramePart part){
        if(!partMap.containsKey(part.originalFrameId)){
            partMap.put(part.originalFrameId, new LinkedList<>());
        }
        partMap.get(part.originalFrameId).add(part);
        counterMap.put(part.originalFrameId,new AtomicInteger(WAIT_TILL_FINISHED+1));

        counterMap.forEach( (k, v) -> v.getAndDecrement());
        counterMap.forEach((k, v)-> {
            if(v.get()<=0) finished.add(k);
        });


        //if(part.isLastPiece) finished.add(part.originalFrameId);

        if(part.isLastPiece && isFullyArrived(part.totalFrameSize,partMap.get(part.originalFrameId))){
            finished.add(part.originalFrameId);
        }
    }

    public List<SegmentFrame> getFinished(){
        List<SegmentFrame> ret = new ArrayList<>(4);
        for(Long id : finished.stream().sorted().collect(Collectors.toList())){
            List<UdpSegmentFramePart> parts = partMap.remove(id);
            counterMap.remove(id);
            if(parts==null) continue;
            if(!isFullyArrived(parts.get(0).totalFrameSize,parts)){
                System.err.println("Not all udp frame arrived, segment (frameId="+parts.get(0).originalFrameId+ ") dropped.");
                continue;
            }
            try {
                ret.add(SegmentFrame.createFromParts(parts));
            } catch (IOException ignored) {
                System.err.println("Cannot create segment");
            }
        }
        finished.clear();
        return ret;
    }

    private static boolean isFullyArrived(int totalSize, List<UdpSegmentFramePart> parts){
        if(totalSize<0){throw new IllegalArgumentException("totalSize must be a non negative number");}
        int arrivedSizeForThisFile = parts.stream().map(p->p.data.length).reduce(Integer::sum).orElse(-1);
        return totalSize<=arrivedSizeForThisFile;
    }
}
