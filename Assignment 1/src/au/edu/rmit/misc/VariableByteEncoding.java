package au.edu.rmit.misc;

import java.util.ArrayList;
import java.util.Arrays;

public class VariableByteEncoding {

    public static Byte[] encode(int value)
    {
        ArrayList<Byte> byteArray = new ArrayList<Byte>();

        while (true)
        {
            byteArray.add(0, (byte) (value % 128));
            if (value < 128) {
                break;
            }
            value = value / 128;
        }

        byteArray.set(byteArray.size()-1, (byte) (byteArray.get(byteArray.size()-1) + 128));
        return byteArray.toArray(new Byte[0]);
    }
            
    public static Integer[] decode(Byte[] bytes)
    {
        ArrayList<Integer> values = new ArrayList<Integer>();
        int value = 0;
        
        for (int i = 0; i < bytes.length; i++)
        {
            if ((bytes[i] & 0xFF) < 128)
            {
                value = 128 * value + (bytes[i] & 0xFF);
            }
            else
            {
                value = 128 * value + (bytes[i] & 0xFF) - 128;
                values.add(value);
                value = 0;
            }   
        }
        return values.toArray(new Integer[0]);
    }
}
