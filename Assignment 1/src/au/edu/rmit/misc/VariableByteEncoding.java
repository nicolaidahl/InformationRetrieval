package au.edu.rmit.misc;

import java.util.ArrayList;
import java.util.Arrays;

public class VariableByteEncoding {

    // Encode an integer into a variable byte array.
    public static Byte[] encode(int value)
    {
        ArrayList<Byte> byteArray = new ArrayList<Byte>();

        // Store 7 bits of integer in the least significant bits of each byte
        while (true)
        {
            byteArray.add(0, (byte) (value % 128));
            if (value < 128) {
                break;
            }
            value = value / 128;
        }

        // Set most significant but of final byte to signal this is the end of the integer representation
        byteArray.set(byteArray.size() - 1, (byte) (byteArray.get(byteArray.size() - 1) + 128));

        return byteArray.toArray(new Byte[0]);
    }

    // Decode byte array of variable byte encoded integers into its constituent parts
    public static Integer[] decode(Byte[] bytes)
    {
        ArrayList<Integer> values = new ArrayList<Integer>();
        int value = 0;
        
        // Note - java sees all byte values as twos complement.
        // Need & 0xFF to correctly convert to unsigned integer.
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
