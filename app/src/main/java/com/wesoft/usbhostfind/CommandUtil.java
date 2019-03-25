package com.wesoft.usbhostfind;

public class CommandUtil {

    public static final byte[] openCommand = {(byte) 0xFF, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    public static final byte[] closeCommand = {(byte) 0xFD, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    public static final byte[] openDevice = {0x53, 0x32, 0x4F, 0x44, 0x32, 0x00, 0x00, 0x00};
}