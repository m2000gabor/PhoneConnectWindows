package hu.elte.sbzbxr.phoneconnect.controller;

import hu.elte.sbzbxr.phoneconnect.model.Picture;

@FunctionalInterface
public interface PictureFunctional {
    void consume(Picture picture);
}
