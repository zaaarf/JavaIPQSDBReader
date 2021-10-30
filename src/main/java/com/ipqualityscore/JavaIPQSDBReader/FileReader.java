package com.ipqualityscore.JavaIPQSDBReader;

import java.io.IOException;
import java.util.ArrayList;
import java.net.InetAddress;
import java.io.RandomAccessFile;
import java.net.UnknownHostException;
import java.util.HashMap;

public class FileReader {
    public IPQSRecord Fetch(String ip) throws IOException {
        IPQSRecord record = new IPQSRecord();

        if(isIPv6() == true && ip.contains(".")){
            throw new IOException("Attemtped to look up IPv4 using IPv6 database file. Aborting.");
        } else if(isIPv6() == false && ip.contains(":")){
            throw new IOException("Attemtped to look up IPv6 using IPv4 database file. Aborting.");
        }

        int position = 0;
        long[] previous = new long[128];
        long file_position = getTreeStart() + 5;
        StringBuilder literal = new StringBuilder(convertIPToBinaryLiteral(ip));

        for(int l = 0;l<257;l++){
            previous[position] = file_position;

            if(literal.length() <= position){
                throw new IOException("Invalid or nonexistent IP address specified for lookup. (EID: 8)");
            }

            byte[] read = new byte[4];
            if(literal.charAt(position) == '0'){
                getHandler().seek(file_position);
                getHandler().read(read);
                file_position = Utility.toUnsignedInt(read);
            } else {
                getHandler().seek(file_position + 4);
                getHandler().read(read);
                file_position = Utility.toUnsignedInt(read);
            }

            if(BlacklistFile == false){
                if(file_position == 0){
                    for(int i = 0; i <= position; i++){
                        if(literal.charAt(position - i) == '1'){
                            literal.setCharAt(position - i, '0');

                            for(int n = (position - i + 1);n < literal.length();n++){
                                literal.setCharAt(n, '1');
                            }

                            position = position - i;
                            file_position = previous[position];
                            break;
                        }
                    }

                    continue;
                }
            }

            if(file_position < getTreeEnd()){
                if(file_position == 0){
                    break;
                }

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

            throw new IOException("Invalid or nonexistent IP address specified for lookup. (EID: 12)");
        }

        throw new IOException("Invalid or nonexistent IP address specified for lookup. (EID: 12)");
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
    private boolean BlacklistFile;
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

    public void setBlacklistFile(boolean blacklistFile){ BlacklistFile = blacklistFile; }

    public boolean getBlacklistFile(){ return BlacklistFile; }

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

    private HashMap<String, String> countrylist;
    public String ConvertCountry(String cc) throws IOException {
        if(countrylist == null){
            countrylist = Utility.GetCountryList();
        }

        return countrylist.get(cc);
    }
}