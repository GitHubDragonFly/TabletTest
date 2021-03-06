package com.e.tablettest;

import android.os.AsyncTask;
import android.util.Log;

import org.libplctag.Tag;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("deprecation")
public class AsyncWriteTaskModbus extends AsyncTask<String, Void, String> {
    private static final String TAG = "Modbus Write Activity";

    private static final BigInteger BigIntegerMin = new BigInteger("-170141183460469231731687303715884105728");
    private static final BigInteger BigIntegerMax = new BigInteger("170141183460469231731687303715884105727");
    private static final BigInteger BigUIntegerMin = new BigInteger("0");
    private static final BigInteger BigUIntegerMax = new BigInteger("340282366920938463463374607431768211455");

    public String value = "";
    private int elem_size, elem_count = 1, strLength;
    private final Tag MBWriteMaster = new Tag();

    WriteTaskCallback WritetaskCallback = MainActivity.WritetaskCallback;

    @Override
    protected String doInBackground(String... params) {
        Log.v(TAG,"On doInBackground...");

        boolean swapBytes = MainActivity.cbSwapBytesChecked;
        boolean swapWords = MainActivity.cbSwapWordsChecked;

        String gateway_unitId = params[0];
        int timeout = Integer.parseInt(params[1]);
        int tag_id = -1, bitIndex = -1;

        while (!isCancelled()){
            String name = params[2].substring(0, params[2].indexOf(';'));

            if (name.startsWith("di") || name.startsWith("ir")){
                value = "MB Write Failed";
                publishProgress();
                MBWriteMaster.close(tag_id);
                Log.v(TAG,"doInBackground Finished");
                return "FINISHED";
            }

            if (name.contains("/")){
                bitIndex = Integer.parseInt(name.substring(name.indexOf('/') + 1));
                name = name.substring(0, name.indexOf('/'));
            }

            String dataType;

            if (params[2].indexOf(';') < params[2].lastIndexOf(';'))
                dataType = params[2].substring(params[2].indexOf(';') + 2, params[2].lastIndexOf(';'));
            else
                dataType = params[2].substring(params[2].indexOf(';') + 2);

            if (dataType.equals("string")){
                strLength = Integer.parseInt(params[2].substring(params[2].lastIndexOf(';') + 2));
                if (bitIndex > 0)
                    bitIndex --;
            }

            switch (dataType) {
                case "bool":
                    elem_size = 1;
                    elem_count = 1;
                    break;
                case "int8":
                case "uint8":
                case "int16":
                case "uint16":
                    elem_size = 2;
                    elem_count = 1;
                    break;
                case "int32":
                case "uint32":
                case "float32":
                case "bool array":
                    elem_size = 2;
                    elem_count = 2;
                    break;
                case "int64":
                case "uint64":
                case "float64":
                    elem_size = 2;
                    elem_count = 4;
                    break;
                case "int128":
                case "uint128":
                    elem_size = 2;
                    elem_count = 8;
                    break;
                case "string":
                    elem_size = 2;
                    elem_count = (int)Math.ceil((float)strLength / 2F);
                    break;
            }

            String tagMBString = "protocol=modbus_tcp&";
            tagMBString += gateway_unitId + "&elem_size=" + elem_size + "&elem_count=" + elem_count + "&name=" + name;

            tag_id = MBWriteMaster.TagCreate(tagMBString, timeout);

            while (MBWriteMaster.getStatus(tag_id) == 1){
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (MBWriteMaster.getStatus(tag_id) == 0){
                MBWriteMaster.read(tag_id, timeout);

                if (bitIndex > -1){
                    int BitValueToWrite = 0;
                    byte[] tempBytes = new byte[]{};

                    if (!dataType.equals("string")){
                        if (params[3].equals("1") || params[3].equals("true") || params[3].equals("True")){
                            BitValueToWrite = 1;
                        } else if (params[3].equals("0") || params[3].equals("false") || params[3].equals("False")){
                            BitValueToWrite = 0;
                        } else {
                            value = "MB Write Failed";
                            publishProgress();
                            MBWriteMaster.close(tag_id);
                            Log.v(TAG,"doInBackground Finished");
                            return "FINISHED";
                        }
                    } else{
                        try {
                            tempBytes = params[3].getBytes("UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        if (tempBytes.length > 1){
                            value = "MB Write Failed";
                            publishProgress();
                            MBWriteMaster.close(tag_id);
                            Log.v(TAG,"doInBackground Finished");
                            return "FINISHED";
                        }
                    }

                    if (dataType.equals("string")){
                        int zeroBytes = 0;
                        byte[] bytes = new byte[elem_size * elem_count];

                        for (int i = 0; i < bytes.length; i++){
                            bytes[i] = (byte)MBWriteMaster.getUInt8(tag_id, i);
                        }

                        byte[] swappedBytes = SwapCheck(bytes);

                        for (byte swappedByte : swappedBytes) {
                            if (swappedByte == 0)
                                zeroBytes += 1;
                            else
                                break;
                        }

                        if (swapBytes){
                            if (swapWords){
                                MBWriteMaster.setUInt8(tag_id, (2 * elem_count) - 1 - bitIndex - zeroBytes, tempBytes[0]);
                            } else
                                MBWriteMaster.setUInt8(tag_id, bitIndex, tempBytes[0]);
                        } else{
                            if (swapWords)
                                MBWriteMaster.setUInt8(tag_id, (2 * elem_count) - bitIndex - zeroBytes, tempBytes[0]);
                            else
                                MBWriteMaster.setUInt8(tag_id, bitIndex + zeroBytes - 1, tempBytes[0]);
                        }
                    } else if (dataType.equals("int128") || dataType.equals("uint128")){
                        int zeroBytes = 0;
                        byte[] bytes = new byte[elem_size * elem_count];

                        for (int i = 0; i < bytes.length; i++){
                            bytes[i] = (byte)MBWriteMaster.getUInt8(tag_id, i);
                        }

                        byte[] swappedBytes = SwapCheck(bytes);

                        for (byte swappedByte : swappedBytes) {
                            if (swappedByte == 0)
                                zeroBytes += 1;
                            else
                                break;
                        }

                        int quot = (int)Math.floor(bitIndex / 8F);

                        if (swapBytes){
                            if (swapWords){
                                MBWriteMaster.setBit(tag_id, 127 - bitIndex - zeroBytes * 8 + quot * 16, BitValueToWrite);
                            } else
                                MBWriteMaster.setBit(tag_id, bitIndex - quot * 16 + zeroBytes * 8 + 8, BitValueToWrite);
                        } else{
                            if (swapWords)
                                MBWriteMaster.setBit(tag_id, 128 - bitIndex - zeroBytes * 8 + quot * 16, BitValueToWrite);
                            else
                                MBWriteMaster.setBit(tag_id, bitIndex - quot * 16 + zeroBytes * 8, BitValueToWrite);
                        }
                    } else {
                        switch (dataType){
                            case "int8":
                            case "uint8":
                                if (swapBytes){
                                    MBWriteMaster.setBit(tag_id, bitIndex + 8, BitValueToWrite);
                                } else {
                                    MBWriteMaster.setBit(tag_id, bitIndex, BitValueToWrite);
                                }
                                break;
                            case "int16":
                            case "uint16":
                                if (swapBytes){
                                    if (bitIndex < 8)
                                        MBWriteMaster.setBit(tag_id, bitIndex + 8, BitValueToWrite);
                                    else
                                        MBWriteMaster.setBit(tag_id, bitIndex - 8, BitValueToWrite);
                                } else {
                                    MBWriteMaster.setBit(tag_id, bitIndex, BitValueToWrite);
                                }
                                break;
                            case "int32":
                            case "uint32":
                            case "float32":
                                if (swapBytes || swapWords){
                                    if (swapBytes){
                                        if (swapWords) // byte3 + byte2 + byte1 + byte0
                                            if (bitIndex < 8)
                                                MBWriteMaster.setBit(tag_id, bitIndex + 24, BitValueToWrite);
                                            else if (bitIndex < 16)
                                                MBWriteMaster.setBit(tag_id, bitIndex + 8, BitValueToWrite);
                                            else if (bitIndex < 24)
                                                MBWriteMaster.setBit(tag_id, bitIndex - 8, BitValueToWrite);
                                            else
                                                MBWriteMaster.setBit(tag_id, bitIndex - 24, BitValueToWrite);
                                        else // byte1 + byte0 + byte3 + byte2
                                            if (bitIndex < 8)
                                                MBWriteMaster.setBit(tag_id, bitIndex + 8, BitValueToWrite);
                                            else if (bitIndex < 16)
                                                MBWriteMaster.setBit(tag_id, bitIndex - 8, BitValueToWrite);
                                            else if (bitIndex < 24)
                                                MBWriteMaster.setBit(tag_id, bitIndex + 8, BitValueToWrite);
                                            else
                                                MBWriteMaster.setBit(tag_id, bitIndex - 8, BitValueToWrite);
                                    } else // byte2 + byte3 + byte0 + byte1
                                        if (bitIndex < 16)
                                            MBWriteMaster.setBit(tag_id, bitIndex + 16, BitValueToWrite);
                                        else
                                            MBWriteMaster.setBit(tag_id, bitIndex - 16, BitValueToWrite);
                                } else {
                                    MBWriteMaster.setBit(tag_id, bitIndex, BitValueToWrite);
                                }
                                break;
                            case "int64":
                            case "uint64":
                            case "float64":
                                if (swapBytes || swapWords){
                                    if (swapBytes){
                                        if (swapWords) // byte7 + byte6 + byte5 + byte4 + byte3 + byte2 + byte1 + byte0
                                            if (bitIndex < 8)
                                                MBWriteMaster.setBit(tag_id, bitIndex + 56, BitValueToWrite);
                                            else if (bitIndex < 16)
                                                MBWriteMaster.setBit(tag_id, bitIndex + 40, BitValueToWrite);
                                            else if (bitIndex < 24)
                                                MBWriteMaster.setBit(tag_id, bitIndex + 24, BitValueToWrite);
                                            else if (bitIndex < 32)
                                                MBWriteMaster.setBit(tag_id, bitIndex + 8, BitValueToWrite);
                                            else if (bitIndex < 40)
                                                MBWriteMaster.setBit(tag_id, bitIndex - 8, BitValueToWrite);
                                            else if (bitIndex < 48)
                                                MBWriteMaster.setBit(tag_id, bitIndex - 24, BitValueToWrite);
                                            else if (bitIndex < 56)
                                                MBWriteMaster.setBit(tag_id, bitIndex - 40, BitValueToWrite);
                                            else
                                                MBWriteMaster.setBit(tag_id, bitIndex - 56, BitValueToWrite);
                                        else // byte1 + byte0 + byte3 + byte2 + byte5 + byte4 + byte7 + byte6
                                            if (bitIndex < 8)
                                                MBWriteMaster.setBit(tag_id, bitIndex + 8, BitValueToWrite);
                                            else if (bitIndex < 16)
                                                MBWriteMaster.setBit(tag_id, bitIndex - 8, BitValueToWrite);
                                            else if (bitIndex < 24)
                                                MBWriteMaster.setBit(tag_id, bitIndex + 8, BitValueToWrite);
                                            else if (bitIndex < 32)
                                                MBWriteMaster.setBit(tag_id, bitIndex - 8, BitValueToWrite);
                                            else if (bitIndex < 40)
                                                MBWriteMaster.setBit(tag_id, bitIndex + 8, BitValueToWrite);
                                            else if (bitIndex < 48)
                                                MBWriteMaster.setBit(tag_id, bitIndex - 8, BitValueToWrite);
                                            else if (bitIndex < 56)
                                                MBWriteMaster.setBit(tag_id, bitIndex + 8, BitValueToWrite);
                                            else
                                                MBWriteMaster.setBit(tag_id, bitIndex - 8, BitValueToWrite);
                                    } else // byte6 + byte7 + byte4 + byte5 + byte2 + byte3 + byte0 + byte1
                                        if (bitIndex < 16)
                                            MBWriteMaster.setBit(tag_id, bitIndex + 48, BitValueToWrite);
                                        else if (bitIndex < 32)
                                            MBWriteMaster.setBit(tag_id, bitIndex + 16, BitValueToWrite);
                                        else if (bitIndex < 48)
                                            MBWriteMaster.setBit(tag_id, bitIndex - 16, BitValueToWrite);
                                        else
                                            MBWriteMaster.setBit(tag_id, bitIndex - 48, BitValueToWrite);
                                } else {
                                    MBWriteMaster.setBit(tag_id, bitIndex, BitValueToWrite);
                                }
                                break;
                        }
                    }
                } else {
                    byte[] bytes, swappedBytes;

                    switch (dataType){
                        case "int8":
                            if (swapBytes)
                                MBWriteMaster.setInt8(tag_id, 1, Integer.parseInt(params[3]));
                            else
                                MBWriteMaster.setInt8(tag_id, 0, Integer.parseInt(params[3]));
                            break;
                        case "uint8":
                            if (swapBytes)
                                MBWriteMaster.setUInt8(tag_id, 1, Short.parseShort(params[3]));
                            else
                                MBWriteMaster.setUInt8(tag_id, 0, Short.parseShort(params[3]));
                            break;
                        case "int16":
                        case "uint16":
                            ByteBuffer bb16 = ByteBuffer.allocate(2);

                            if (swapBytes){
                                bb16.order(ByteOrder.LITTLE_ENDIAN);

                                if (dataType.equals("int16"))
                                    bytes = bb16.putShort(Short.parseShort(params[3])).array();
                                else
                                    bytes = bb16.putShort((short)Integer.parseInt(params[3])).array();

                                for (int z = 0; z < 2; z++) {
                                    MBWriteMaster.setUInt8(tag_id, z, bytes[z]);
                                }
                            } else {
                                if (dataType.equals("int16"))
                                    MBWriteMaster.setInt16(tag_id, 0, Integer.parseInt(params[3]));
                                else
                                    MBWriteMaster.setUInt16(tag_id, 0, Integer.parseInt(params[3]));
                            }

                            break;
                        case "int32":
                        case "uint32":
                        case "float32":
                            if (swapBytes || swapWords){
                                ByteBuffer bb32 = ByteBuffer.allocate(8);

                                if (swapBytes){
                                    if (swapWords) { // byte3 + byte2 + byte1 + byte0
                                        bb32.order(ByteOrder.BIG_ENDIAN);

                                        if (dataType.equals("int32")){
                                            bytes = bb32.putInt(Integer.parseInt(params[3])).array();
                                        } else if (dataType.equals("float32")){
                                            bytes = bb32.putFloat(Float.parseFloat(params[3])).array();
                                        } else {
                                            bytes = bb32.putLong(Long.parseLong(params[3])).array();
                                        }

                                        // Swap words
                                        swappedBytes = SwapWords(bytes);

                                        for (int z = 0; z < swappedBytes.length; z++) {
                                            MBWriteMaster.setUInt8(tag_id, z, swappedBytes[z]);
                                        }
                                    }
                                    else { // byte1 + byte0 + byte3 + byte2
                                        bb32.order(ByteOrder.LITTLE_ENDIAN);

                                        if (dataType.equals("int32")){
                                            bytes = bb32.putInt(Integer.parseInt(params[3])).array();
                                        } else if (dataType.equals("float32")){
                                            bytes = bb32.putFloat(Float.parseFloat(params[3])).array();
                                        } else {
                                            bytes = bb32.putLong(Long.parseLong(params[3])).array();
                                        }

                                        // Swap bytes
                                        swappedBytes = SwapBytes(bytes);

                                        for (int z = 0; z < swappedBytes.length; z++) {
                                            MBWriteMaster.setUInt8(tag_id, z, swappedBytes[z]);
                                        }
                                    }
                                } else { // byte2 + byte3 + byte0 + byte1
                                    bb32.order(ByteOrder.BIG_ENDIAN);

                                    if (dataType.equals("int32")){
                                        bytes = bb32.putInt(Integer.parseInt(params[3])).array();
                                    } else if (dataType.equals("float32")){
                                        bytes = bb32.putFloat(Float.parseFloat(params[3])).array();
                                    } else {
                                        bytes = bb32.putLong(Long.parseLong(params[3])).array();
                                    }

                                    // Swap bytes
                                    swappedBytes = SwapBytes(bytes);

                                    for (int z = 0; z < swappedBytes.length; z++) {
                                        MBWriteMaster.setUInt8(tag_id, z, swappedBytes[z]);
                                    }
                                }
                            } else {
                                if (dataType.equals("int32")){
                                    MBWriteMaster.setInt32(tag_id, 0, Integer.parseInt(params[3]));
                                } else if (dataType.equals("float32")){
                                    MBWriteMaster.setFloat32(tag_id, 0, Float.parseFloat(params[3]));
                                } else {
                                    MBWriteMaster.setUInt32(tag_id, 0, Long.parseLong(params[3]));
                                }
                            }
                            break;
                        case "int64":
                        case "uint64":
                        case "float64":
                            if (swapBytes || swapWords){
                                ByteBuffer bb64 = ByteBuffer.allocate(8);

                                if (swapBytes){
                                    if (swapWords) { // byte7 + byte6 + byte5 + byte4 + byte3 + byte2 + byte1 + byte0
                                        bb64.order(ByteOrder.BIG_ENDIAN);

                                        if (dataType.equals("int64")){
                                            bytes = bb64.putLong(Long.parseLong(params[3])).array();
                                        } else if (dataType.equals("float64")){
                                            bytes = bb64.putDouble(Double.parseDouble(params[3])).array();
                                        } else {
                                            bytes = bb64.putLong(new BigInteger(params[3]).longValue()).array();
                                        }

                                        // Swap words
                                        swappedBytes = SwapWords(bytes);

                                        for (int z = 0; z < swappedBytes.length; z++) {
                                            MBWriteMaster.setUInt8(tag_id, z, swappedBytes[z]);
                                        }
                                    }
                                    else { // byte1 + byte0 + byte3 + byte2 + byte5 + byte4 + byte7 + byte6
                                        bb64.order(ByteOrder.LITTLE_ENDIAN);

                                        if (dataType.equals("int64")){
                                            bytes = bb64.putLong(Long.parseLong(params[3])).array();
                                        } else if (dataType.equals("float64")){
                                            bytes = bb64.putDouble(Double.parseDouble(params[3])).array();
                                        } else {
                                            bytes = bb64.putLong(new BigInteger(params[3]).longValue()).array();
                                        }

                                        // Swap bytes
                                        swappedBytes = SwapBytes(bytes);

                                        for (int z = 0; z < swappedBytes.length; z++) {
                                            MBWriteMaster.setUInt8(tag_id, z, swappedBytes[z]);
                                        }
                                    }
                                } else { // byte6 + byte7 + byte4 + byte5 + byte2 + byte3 + byte0 + byte1
                                    bb64.order(ByteOrder.BIG_ENDIAN);

                                    if (dataType.equals("int64")){
                                        bytes = bb64.putLong(Long.parseLong(params[3])).array();
                                    } else if (dataType.equals("float64")){
                                        bytes = bb64.putDouble(Double.parseDouble(params[3])).array();
                                    } else {
                                        bytes = bb64.putLong(new BigInteger(params[3]).longValue()).array();
                                    }

                                    // Swap bytes
                                    swappedBytes = SwapBytes(bytes);

                                    for (int z = 0; z < swappedBytes.length; z++) {
                                        MBWriteMaster.setUInt8(tag_id, z, swappedBytes[z]);
                                    }
                                }
                            } else {
                                if (dataType.equals("int64")){
                                    MBWriteMaster.setInt64(tag_id, 0, Long.parseLong(params[3]));
                                } else if (dataType.equals("float64")){
                                    MBWriteMaster.setFloat64(tag_id, 0, Double.parseDouble(params[3]));
                                } else {
                                    MBWriteMaster.setUInt64(tag_id, 0, new BigInteger(params[3]));
                                }
                            }
                            break;
                        case "int128":
                            BigInteger value2write = new BigInteger(params[3]);
                            int comp1 = value2write.compareTo(BigIntegerMin);
                            int comp2 = value2write.compareTo(BigIntegerMax);

                            if (comp1 < 0 && comp2 > 0){
                                value = "MB Write Failed";
                                publishProgress();
                                MBWriteMaster.close(tag_id);
                                Log.v(TAG,"doInBackground Finished");
                                return "FINISHED";
                            } else{
                                byte[] value2writeBytes = value2write.toByteArray();
                                byte[] valueBytes = new byte[16];

                                System.arraycopy(value2writeBytes, 0, valueBytes, 16 - value2writeBytes.length, value2writeBytes.length);

                                swappedBytes = SwapCheck(valueBytes);

                                for (int k = 0; k < swappedBytes.length; k++){
                                    MBWriteMaster.setUInt8(tag_id, k, swappedBytes[k]);
                                }
                            }

                            break;
                        case "uint128":
                            BigInteger uvalue2write = new BigInteger(params[3]);
                            int ucomp1 = uvalue2write.compareTo(BigUIntegerMin);
                            int ucomp2 = uvalue2write.compareTo(BigUIntegerMax);

                            if (ucomp1 < 0 && ucomp2 > 0){
                                value = "MB Write Failed";
                                publishProgress();
                                MBWriteMaster.close(tag_id);
                                Log.v(TAG,"doInBackground Finished");
                                return "FINISHED";
                            } else{
                                byte[] uvalue2writeBytes = uvalue2write.toByteArray();
                                byte[] uvalueBytes = new byte[16];

                                System.arraycopy(uvalue2writeBytes, 0, uvalueBytes, 16 - uvalue2writeBytes.length, uvalue2writeBytes.length);

                                swappedBytes = SwapCheck(uvalueBytes);

                                for (int k = 0; k < swappedBytes.length; k++){
                                    MBWriteMaster.setUInt8(tag_id, k, swappedBytes[k]);
                                }
                            }
                            break;
                        case "bool":
                            MBWriteMaster.setBit(tag_id, 0, Integer.parseInt(params[3]));
                            break;
                        case "string":
                            bytes = new byte[elem_size * elem_count];
                            byte[] tempBytes = new byte[]{};

                            try {
                                tempBytes = params[3].getBytes("UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                            System.arraycopy(tempBytes, 0, bytes, 0, tempBytes.length);

                            swappedBytes = SwapCheck(bytes);

                            for (int z = 0; z < swappedBytes.length; z++) {
                                MBWriteMaster.setUInt8(tag_id, z, swappedBytes[z]);
                            }

                            break;
                    }
                }

                MBWriteMaster.write(tag_id, timeout);

                value = "MB Write Success";
            } else {
                value = "MB Write Failed";
            }

            publishProgress();
        }

        MBWriteMaster.close(tag_id);

        Log.v(TAG,"doInBackground Finished");

        return "FINISHED";
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.v(TAG,"On PreExecute...");
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate();

        WritetaskCallback.WriteUpdateUI(value);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        Log.v(TAG,"On PostExecute...");
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        Log.v(TAG,"On Cancelled...");
    }

    private byte[] SwapCheck(byte[] bytes)
    {
        if (MainActivity.cbSwapWordsChecked && bytes.length > 3)
        {
            for (int i = 0; i < bytes.length / 2; i++)
            {
                byte tempByte = bytes[i];
                bytes[i] = bytes[bytes.length - i - 1];
                bytes[bytes.length - i - 1] = tempByte;
            }
        }

        if (!MainActivity.cbSwapBytesChecked)
        {
            for (int i = 0; i < bytes.length; i += 2)
            {
                byte tempByte = bytes[i];
                bytes[i] = bytes[i + 1];
                bytes[i + 1] = tempByte;
            }
        }

        return bytes;
    }

    private byte[] SwapBytes(byte[] bytes)
    {
        for (int i = 0; i < bytes.length; i += 2)
        {
            byte tempByte = bytes[i];
            bytes[i] = bytes[i + 1];
            bytes[i + 1] = tempByte;
        }

        return bytes;
    }

    private byte[] SwapWords(byte[] bytes)
    {
        for (int i = 0; i < bytes.length / 2; i++)
        {
            byte tempByte = bytes[i];
            bytes[i] = bytes[bytes.length - i - 1];
            bytes[bytes.length - i - 1] = tempByte;
        }

        return bytes;
    }
}
