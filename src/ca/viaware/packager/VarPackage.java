package ca.viaware.packager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class VarPackage {

							//**TI83F*										Magic Numbers
	static byte[] header = {0x2A, 0x2A, 0x54, 0x49, 0x38, 0x33, 0x46, 0x2A, 0x1A, 0x0A, 0x00};
	
	public static void main(String[] args) {
		if (args.length < 3) {
			System.err.println("Usage: <infile> <outfile> <varname>");
			return;
		}
		File inFile = new File(args[0]);
		File outFile = new File(args[1]);
		
		try {
			InputStream in = new FileInputStream(inFile);
			if (!outFile.exists()) outFile.createNewFile();
			OutputStream out = new FileOutputStream(outFile);
			
			out.write(header);
			for (int i = 0; i < 42; i++) {
				out.write(0x30); //Fill gimpy comment section with zeroes
			}
			
			//2byte Length int
			int dataSize = (int) (17 + 2 + inFile.length());
			out.write(dataSize);
			out.write(dataSize >> 8);
			
			//BEGIN VARIABLE ENTRY SEGMENT
			
			int checksum = 0;
			
			//Magic numbers
			checksum += write(out, 0x0D);
			checksum += write(out, 0x00);
			
			//Var data size
			int varSize = (int) (2 + inFile.length());
			checksum += write(out, varSize);
			checksum += write(out, varSize >> 8);
			
			//Edit locked prog
			checksum += write(out, 0x06);
			
			//Write name
			String name = args[2].toUpperCase();
			if (name.length() > 8) name = name.substring(0, 8);
			char[] nameChars = name.toCharArray();
			for (char c : nameChars) checksum += write(out, c);
			for (int i = 0; i < (8 - nameChars.length); i++) {
				checksum += write(out, 0x00);
			}
			
			//Set version to zero
			checksum += write(out, 0x00);
			
			//Set not archived
			checksum += write(out, 0x00);
			
			//Add var length again
			checksum += write(out, varSize);
			checksum += write(out, varSize >> 8);
			
			//BEGIN VARIABLE DATA SEGMENT
			
			int fileSize = (int) inFile.length();
			checksum += write(out, fileSize);
			checksum += write(out, fileSize >> 8);
			
			int read;
			while ((read = in.read()) != -1) {
				checksum += write(out, read);
			}
			
			out.write(checksum);
			out.write(checksum >> 8);
			
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static int write(OutputStream out, int b) throws IOException {
		out.write(b);
		return b & 0xFF;
	}
	
}
