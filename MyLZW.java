import java.util.Arrays;
/*************************************************************************
 *  Compilation:  javac LZW.java
 *  Execution:    java LZW - < input.txt   (compress)
 *  Execution:    java LZW + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *
 *  Compress or expand binary input from standard input using LZW.
 *
 *  WARNING: STARTING WITH ORACLE JAVA 6, UPDATE 7 the SUBSTRING
 *  METHOD TAKES TIME AND SPACE LINEAR IN THE SIZE OF THE EXTRACTED
 *  SUBSTRING (INSTEAD OF CONSTANT SPACE AND TIME AS IN EARLIER
 *  IMPLEMENTATIONS).
 *
 *  See <a href = "http://java-performance.info/changes-to-string-java-1-7-0_06/">this article</a>
 *  for more details.
 *
 *************************************************************************/

public class MyLZW {
    private static final int R = 256;        // number of input chars
    private static int L = 512;       // number of codewords = 2^W
    private static int W = 9;         // codeword width

    private static int oldSize=0;	// original size of bits
    private static int newSize=0;	//compressed size of bits
    private static boolean monitorActive=false;	//tells if monitoring compression
    private static float oldRatio=1;	//compression ratio of original monitor

    public static void compress(String mode) {
        String input = BinaryStdIn.readString();
        TST<Integer> st = new TST<Integer>();
        if(!mode.equals("r")&&!mode.equals("n")&&!mode.equals("m")){
            System.err.println("Invalid mode");
            return;
        }

        float newRatio=1;	//current compression ration of monitor

        for (int i = 0; i < R; i++)
            st.put("" + (char) i, i);
        int code = R+1;  // R is codeword for EOF

        BinaryStdOut.write(mode, 8);

        while (input.length() > 0) {
            String s = st.longestPrefixOf(input);  // Find max prefix match s.
            BinaryStdOut.write(st.get(s), W);      // Print s's encoding.

            newSize+=W;

            int t = s.length();
            oldSize+=t;

            if(code >=L && W<16){	//if codewords reach capacity
            	W++;
            	L*=2;
            }
            if(code >=L && W==16 && mode.equalsIgnoreCase("m") && (!monitorActive)){
            	monitorActive=true;
            	oldRatio= (float)oldSize/(float)newSize;
            }
            newRatio= (float)oldSize/(float)newSize;
            if(code == L && W==16 && (mode.equalsIgnoreCase("r") ||
            		(mode.equalsIgnoreCase("m") &&monitorActive &&(oldRatio/newRatio)>1.1))){
            	L = 512;
            	W = 9;
            	st = new TST<Integer>();
                for (int j = 0; j < R; j++)
                    st.put("" + (char) j, j);
                code = R+1;
                monitorActive=false;
            }

            if (t < input.length() && code < L)    // Add s to symbol table.
                st.put(input.substring(0, t + 1), code++);
            input = input.substring(t);            // Scan past s in input.
        }
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    }

    public static void expand() {
        String[] st = new String[L];
        int i; // next available codeword value
        float newRatio=1;

        // initialize symbol table with all 1-character strings
        for (i = 0; i < R; i++)
            st[i] = "" + (char) i;
        st[i++] = "";                        // (unused) lookahead for EOF

        String mode=""+(char)BinaryStdIn.readInt(8);

        int codeword = BinaryStdIn.readInt(W);
        if (codeword == R) return;           // expanded message is empty string
        String val = st[codeword];
        newSize+=W;

        while (true) {
            BinaryStdOut.write(val);
            oldSize+=val.length();
            if(i >=L && W<16){
            	W++;
            	System.err.println("Length is now: "+ W);
            	L*=2;
            	st=Arrays.copyOf(st, L);
            }
            if(i== L && W==16 && mode.equalsIgnoreCase("m") && !(monitorActive)){
                monitorActive=true;
                oldRatio=(float)oldSize/(float)newSize;
            }
                newRatio= (float)oldSize/(float)newSize;
            if(i == L && W==16 && (mode.equalsIgnoreCase("r")||
            		(mode.equalsIgnoreCase("m") && monitorActive &&(oldRatio/newRatio)>1.1))){
            	L = 512;
            	W = 9;
            	st = new String[L];
                for (int j = 0; j < R; j++)
                    st[j]=""+ (char) j;
                i = R;
                st[i] ="";
                i++;
                monitorActive=false;
                System.err.println("Reset codebook!: ");
            }
            codeword = BinaryStdIn.readInt(W);
            newSize+=W;
            if (codeword == R) break;
            String s = st[codeword];
            if (i == codeword) s = val + val.charAt(0);   // special case hack
            if (i < L) st[i++] = val + s.charAt(0);
            val = s;
        }
        BinaryStdOut.close();
    }



    public static void main(String[] args) {
        if      (args[0].equals("-")) compress(args[1]);
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }

}
