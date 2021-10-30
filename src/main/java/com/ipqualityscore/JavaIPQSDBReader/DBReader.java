package com.ipqualityscore.JavaIPQSDBReader;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class DBReader {
    public static FileReader Open(String filename) throws IOException {
        FileReader reader = new FileReader();
        reader.setValid(false);

        reader.setHandler((new RandomAccessFile(filename, "r")));

        Bitmask fb = Bitmask.create(reader.getHandler().read()); // Byte 0
        reader.setBinaryData(fb.Has(Bitmask.BinaryData));
        if(fb.Has(Bitmask.IPv4Map)){
            reader.setValid(true);
            reader.setIPv6(false);
        }

        if(fb.Has(Bitmask.IPv6Map)){
            reader.setValid(true);
            reader.setIPv6(true);
        }

        if(fb.Has(Bitmask.BlacklistData)){
            reader.setBlacklistFile(true);
        }

        if(reader.isValid() == false){
            throw new IOException("Invalid file format, invalid first byte, EID 1.");
        }

        if(Bitmask.create(reader.getHandler().read()).Has(Bitmask.ReaderVersion) == false){ // Byte 1
            throw new IOException("Invalid file version, invalid header bytes, EID 1.");
        }

        int[] hbd = {reader.getHandler().readUnsignedByte(), reader.getHandler().readUnsignedByte(), reader.getHandler().readUnsignedByte()}; // Byte 2 - 4
        long headerbytes = Utility.uVarInt(hbd);
        if(headerbytes == 0){
            throw new IOException("Invalid file format, invalid record bytes, EID 2.");
        }

        int[] rbd = {reader.getHandler().readUnsignedByte(), reader.getHandler().readUnsignedByte()}; // Byte 5 - 6
        reader.setRecordBytes(Utility.uVarInt(rbd));
        if(reader.getRecordBytes() == 0){
            throw new IOException("Invalid file format, invalid record bytes, EID 3.");
        }

        byte[] var = {reader.getHandler().readByte(), reader.getHandler().readByte(), reader.getHandler().readByte(), reader.getHandler().readByte()}; // Byte 7 - 10
        reader.setTotalBytes(Utility.toUnsignedInt(var));
        if(reader.getTotalBytes() == 0){
            throw new IOException("Invalid file format, EID 4.");
        }

        reader.setTreeStart(headerbytes);
        int columnlength = (int) (headerbytes - 11);
        byte[] columns = new byte[columnlength];
        reader.getHandler().read(columns);

        int totalcolumns = (int) (((headerbytes) - 11) / 24);
        for(int i = 0; i < totalcolumns; i++){
            byte[] descriptionraw = Arrays.copyOfRange(columns, (i*24),((i+1)*24)-2);

            Column n = new Column();
            n.setName((new String(descriptionraw).replaceAll("\0", "")));
            n.setType(Bitmask.create(Byte.toUnsignedInt(columns[((i+1)*24)-1])));
            reader.getColumns().add(n);
        }

        if(totalcolumns == 0){
            throw new IOException("File does not appear to be valid, no column data found. EID: 5");
        }

        if(Bitmask.create(reader.getHandler().read()).Has(Bitmask.TreeData) == false){
            throw new IOException("File does not appear to be valid, bad binary tree. EID: 6");
        }

        byte[] treelength = {reader.getHandler().readByte(), reader.getHandler().readByte(), reader.getHandler().readByte(), reader.getHandler().readByte()};
        reader.setTreeEnd(reader.getTreeStart() + Utility.toUnsignedInt(treelength));
        if(reader.getTreeEnd() == 0){
            throw new IOException("File does not appear to be valid, tree size is too small. EID: 7");
        }

        return reader;
    }


}