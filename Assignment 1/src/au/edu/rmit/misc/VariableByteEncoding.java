package au.edu.rmit.misc;

import java.util.ArrayList;
import java.util.Arrays;

public class VariableByteEncoding {

    public static void main(String[] args) {

        ArrayList<Byte> byteArray = new ArrayList<Byte>();
        byteArray.addAll(Arrays.asList(VariableByteEncoding.encode(84058)));
        byteArray.addAll(Arrays.asList(VariableByteEncoding.encode(5)));
        byteArray.addAll(Arrays.asList(VariableByteEncoding.encode(214577)));

        for (int i = 0; i < byteArray.size(); i++)
        {
            System.out.println(Integer.toBinaryString(byteArray.get(i) & 0xFF)); 
        }
        
        for (Integer value : VariableByteEncoding.decode(byteArray.toArray(new Byte[0])))
            System.out.println(value);

    }

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
            
    /*VBENCODE(numbers)
    1 bytestream ← hi
    2 for each n ∈ numbers
    3 do bytes ← VBENCODENUMBER(n)
    4 bytestream ← EXTEND(bytestream, bytes)
    5 return bytestream*/

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
