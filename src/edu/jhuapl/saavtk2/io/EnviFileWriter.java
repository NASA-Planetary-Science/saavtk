package edu.jhuapl.saavtk2.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import edu.jhuapl.saavtk.util.ImageDataUtil;
import vtk.vtkImageData;

public class EnviFileWriter implements ImageDataWriter {

	public static enum InterleaveType
	{
		bsq,bil,bip;
	}
	
	public static enum ByteOrder
	{
		host(0),network(1);
		
		
		int enviFlag;
		private ByteOrder(int enviFlag) {
			this.enviFlag=enviFlag;
		}
		
	}

	
	InterleaveType interleaveType;
	ByteOrder byteOrder;

	public EnviFileWriter() {
        this(InterleaveType.bsq, ByteOrder.host);
	}
	
	private EnviFileWriter(InterleaveType interleaveType, ByteOrder byteOrder) {
		this.interleaveType=interleaveType;
		this.byteOrder=byteOrder;
	}
	
	
	@Override
	public void write(vtkImageData data, File file) {
		try {
			// Check if interleave type is recognized
			switch (interleaveType) {
			case bsq:
			case bil:
			case bip:
				break;
			default:
				System.out.println("Interleave type " + interleaveType + " unrecognized, aborting exportAsEnvi()");
				return;
			}

			//String basePath=FilenameUtils.getPrefix(file.getAbsolutePath());
			String hdrFile=file.toPath().resolveSibling(file.getName()+".hdr").toString();
			String enviFile=file.toPath().resolveSibling(file.getName()+".envi").toString();
			
			// Create output stream for header (.hdr) file
			FileOutputStream fs = null;
			try {
				fs = new FileOutputStream(hdrFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			}
			OutputStreamWriter osw = new OutputStreamWriter(fs);
			BufferedWriter out = new BufferedWriter(osw);

			
			int imageWidth = data.GetDimensions()[0];
			int imageHeight = data.GetDimensions()[1];
			int imageDepth = data.GetDimensions()[2];

			// Write the fields of the header
			out.write("ENVI\n");
			out.write("samples = " + imageWidth + "\n");
			out.write("lines = " + imageHeight + "\n");
			out.write("bands = " + imageDepth + "\n");
			out.write("header offset = " + "0" + "\n");
			out.write("data type = " + "4" + "\n"); // 1 = byte, 2 = int, 3 =
													// signed
													// int, 4 = float
			out.write("interleave = " + interleaveType.name() + "\n"); // bsq = band
																// sequential,
																// bil =
																// band
																// interleaved
																// by line, bip
																// =
																// band
																// interleaved
																// by pixel
			out.write("byte order = "); // 0 = host(intel, LSB first), 1 =
										// network
										// (IEEE, MSB first)
			
				out.write(byteOrder.enviFlag + "\n");
			out.write(getEnviHeaderAppend());
			out.close();

			// Configure byte buffer & endianess
			ByteBuffer bb = ByteBuffer.allocate(4 * imageWidth * imageHeight * imageDepth); // 4
																							// bytes
																							// per
							
			switch (byteOrder) {
			case host:
				bb.order(java.nio.ByteOrder.LITTLE_ENDIAN);
				
				break;
			case network:
				bb.order(java.nio.ByteOrder.BIG_ENDIAN);
				break;
			default:
				break;
			}
			
			// Write pixels to byte buffer
			// Remember, VTK origin is at bottom left while ENVI origin is at
			// top
			// left
			float[][][] imageData = ImageDataUtil.vtkImageDataToArray3D(data);

			switch (interleaveType) {
			case bsq:
				// Band sequential: col, then row, then depth
				for (int depth = 0; depth < imageDepth; depth++) {
					// for(int row = imageHeight-1; row >= 0; row--)
					for (int row = 0; row < imageHeight; row++) {
						for (int col = 0; col < imageWidth; col++) {
							bb.putFloat(imageData[depth][row][col]);
						}
					}
				}
				break;
			case bil:
				// Band interleaved by line: col, then depth, then row
				// for(int row=imageHeight-1; row >= 0; row--)
				for (int row = 0; row < imageHeight; row++) {
					for (int depth = 0; depth < imageDepth; depth++) {
						for (int col = 0; col < imageWidth; col++) {
							bb.putFloat(imageData[depth][row][col]);
						}
					}
				}
				break;
			case bip:
				// Band interleaved by pixel: depth, then col, then row

			//				// for(int row=imageHeight-1; row >= 0; row--)
				for (int row = 0; row < imageHeight; row++) {
					for (int col = 0; col < imageWidth; col++) {
						for (int depth = 0; depth < imageDepth; depth++) {
							bb.putFloat(imageData[depth][row][col]);
						}
					}
				}
				break;
			}

			// Create output stream and write contents of byte buffer
			FileChannel fc = null;
			FileOutputStream stream = new FileOutputStream(enviFile);
			fc = stream.getChannel();
			bb.flip(); // flip() is a misleading name, nothing is being flipped.
			// Buffer end is set to curr pos and curr pos set to
			// beginning.
			fc.write(bb);
			fc.close();
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	protected String getEnviHeaderAppend() {
		return "";
	}

}
