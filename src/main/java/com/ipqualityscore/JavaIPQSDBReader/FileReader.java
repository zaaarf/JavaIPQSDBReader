package com.ipqualityscore.JavaIPQSDBReader;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class FileReader {
    public IPQSRecord Fetch(String ip) throws IOException {
        IPQSRecord record = new IPQSRecord();

        if(isIPv6() == true && ip.contains(".")){
            throw new IOException("Attemtped to look up IPv4 using IPv6 database file. Aborting.");
        } else if(isIPv6() == false && ip.contains(":")){
            throw new IOException("Attemtped to look up IPv6 using IPv4 database file. Aborting.");
        }

        String literal = convertIPToBinaryLiteral(ip);
        int position = 0;

        long file_position = getTreeStart() + 5;
        while(true){
            if(literal.length() <= position){
                throw new IOException("Invalid or nonexistant IP address specified for lookup. (EID: 8)");
            }

            byte[] read = new byte[4];
            if(literal.charAt(position) == '0'){
                getHandler().seek(file_position);
            } else {
                getHandler().seek(file_position + 4);
            }

            getHandler().read(read);

            long next = Utility.toUnsignedInt(read);

            if(next == 0){
                if(literal.charAt(position) == '1'){
                    getHandler().seek(file_position);
                    getHandler().read(read);

                    next = Utility.toUnsignedInt(read);
                    if(next == 0){
                        throw new IOException("Invalid or nonexistant IP address specified for lookup. (EID: 10)");
                    }
                } else {
                    getHandler().seek(file_position + 4);
                    getHandler().read(read);

                    next = Utility.toUnsignedInt(read);
                    if(next == 0){
                        throw new IOException("Invalid or nonexistant IP address specified for lookup. (EID: 9)");
                    }
                }
            }

            file_position = next;
            if(file_position < getTreeEnd()){
                position++;
                continue;
            }

            // In theory we're at a record.
            byte[] raw = new byte[(int) getRecordBytes()];
            getHandler().seek(file_position);
            getHandler().read(raw);

            if(record.parse(this, raw)){
                return record;
            }

            throw new IOException("Invalid or nonexistant IP address specified for lookup. (EID: 12)");
        }
    }

    private String convertIPToBinaryLiteral(String ip) throws UnknownHostException {
        String result = "";
        for (byte b : InetAddress.getByName(ip).getAddress()) {
            result = result + String.format("%8s", Integer.toBinaryString(b)).replace(" ", "0");
        }

        return result;
    }

    private RandomAccessFile Handler;
    private long TotalBytes;
    private long RecordBytes;
    private long TreeStart;
    private long TreeEnd;
    private boolean IPv6;
    private boolean Valid;
    private boolean BinaryData;
    private ArrayList<Column> Columns = new ArrayList<Column>();

    public RandomAccessFile getHandler() {
        return Handler;
    }

    public void setHandler(RandomAccessFile handler) {
        Handler = handler;
    }

    public long getTotalBytes() {
        return TotalBytes;
    }

    public void setTotalBytes(long totalBytes) {
        TotalBytes = totalBytes;
    }

    public long getRecordBytes() {
        return RecordBytes;
    }

    public void setRecordBytes(long recordBytes) {
        RecordBytes = recordBytes;
    }

    public long getTreeStart() {
        return TreeStart;
    }

    public void setTreeStart(long treeStart) {
        TreeStart = treeStart;
    }

    public long getTreeEnd() {
        return TreeEnd;
    }

    public void setTreeEnd(long treeEnd) {
        TreeEnd = treeEnd;
    }

    public boolean isIPv6() {
        return IPv6;
    }

    public void setIPv6(boolean IPv6) {
        this.IPv6 = IPv6;
    }

    public boolean isValid() {
        return Valid;
    }

    public void setValid(boolean valid) {
        Valid = valid;
    }

    public boolean hasBinaryData() {
        return BinaryData;
    }

    public void setBinaryData(boolean binaryData) {
        BinaryData = binaryData;
    }

    public ArrayList<Column> getColumns() {
        return Columns;
    }

    public void setColumns(ArrayList<Column> columns) {
        Columns = columns;
    }
}