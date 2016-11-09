package com.syk.sm.datafetch.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.syk.sm.bean.BseBhavCopyBean;
import com.syk.sm.utility.SM_Utilities;

public class Read_DailyBhavCopy_FromBSE {

	public static ArrayList<BseBhavCopyBean> getBhavCopyForDate(String dateStr) throws Exception {
		ArrayList<BseBhavCopyBean> bhavCopyBeansList = new ArrayList<BseBhavCopyBean>();

		// create temp directory is not exists
		File folder = new File(SM_Utilities.getSMProperty("tempFolder"));
		if (!folder.exists()) {
			folder.mkdir();
		}

		if (downloadZipFileFromBse(dateStr)) {
			readEQFile(dateStr, bhavCopyBeansList);
		}

		SM_Utilities.log("Done - getBhavCopyForDate(" + dateStr + ")");
		return bhavCopyBeansList;
	}

	private static void readEQFile(String dateStr, ArrayList<BseBhavCopyBean> bhavCopyBeansList) {
		try {
			String fileLoc = SM_Utilities.getSMProperty("tempFolder") + "\\" + ((SM_Utilities.getSMProperty("bhavFile")).replaceAll("@DATE@", dateStr));
			File csvFile = new File(fileLoc);
			Scanner scanner = new Scanner(new FileInputStream(csvFile));
			Scanner delimitedScanner = scanner.useDelimiter("\\n");
			if (delimitedScanner.hasNext()) {
				delimitedScanner.next();
				// skip first row
			}
			while (delimitedScanner.hasNext()) {
				String bhavCopyRow = delimitedScanner.next();
				if (bhavCopyRow != null && bhavCopyRow.trim().length() > 1) {
					String bhavCopyCols[] = bhavCopyRow.split(",");

					BseBhavCopyBean bhavCopyBean = new BseBhavCopyBean();
					bhavCopyBean.setScripCode(Integer.parseInt(bhavCopyCols[0].trim()));
					bhavCopyBean.setCompanyName(bhavCopyCols[1].trim());
					bhavCopyBean.setScripGroup(bhavCopyCols[2].trim());
					bhavCopyBean.setScripType(bhavCopyCols[3].trim());
					bhavCopyBean.setDaysOpen(Double.parseDouble(bhavCopyCols[4].trim()));
					bhavCopyBean.setDaysHigh(Double.parseDouble(bhavCopyCols[5].trim()));
					bhavCopyBean.setDaysLow(Double.parseDouble(bhavCopyCols[6].trim()));
					bhavCopyBean.setDaysClose(Double.parseDouble(bhavCopyCols[7].trim()));
					bhavCopyBean.setDaysLast(Double.parseDouble(bhavCopyCols[8].trim()));
					bhavCopyBean.setPrevDaysClose(Double.parseDouble(bhavCopyCols[9].trim()));
					bhavCopyBean.setNoOfTrades(Long.parseLong(bhavCopyCols[10].trim()));
					bhavCopyBean.setNoOfShares(Long.parseLong(bhavCopyCols[11].trim()));
					bhavCopyBean.setNetTurnOver(Double.parseDouble(bhavCopyCols[12].trim()));
					if (bhavCopyBean.getScripType() != null && bhavCopyBean.getScripType().equals("Q")) {
						bhavCopyBeansList.add(bhavCopyBean);
					}

				}

			}
			scanner.close();
			delimitedScanner.close();
		} catch (Exception ex) {
			SM_Utilities.logConsole("Read_DailyBhavCopy_FromBSE | readEQFile | Exception: " + ex);
			ex.printStackTrace();
		}
	}

	private static boolean downloadZipFileFromBse(String dateStr) throws Exception {
		boolean zipFileFound = true;
		try {
			// get the zip file content
			String zipFileLoc = SM_Utilities.getSMProperty("tempFolder") + "\\" + ((SM_Utilities.getSMProperty("bhavZipFile")).replaceAll("@DATE@", dateStr));
			File zipFile = new File(zipFileLoc);

			InputStream inStr = SM_Utilities.getURLContentAsStream((SM_Utilities.getSMProperty("bhavCopyURL")).replaceAll("@DATE@", dateStr));
			ReadableByteChannel rbc = Channels.newChannel(inStr);
			FileOutputStream fosZip = new FileOutputStream(zipFile);
			fosZip.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fosZip.close();
			inStr.close();

			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
			// get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			if (ze != null) {
				while (ze != null) {
					String fileName = ze.getName();
					File newFile = new File(SM_Utilities.getSMProperty("tempFolder") + File.separator + fileName);

					SM_Utilities.log("File unzipped : " + newFile.getAbsoluteFile());

					// new File(newFile.getParent()).mkdirs();

					FileOutputStream fos = new FileOutputStream(newFile);

					int len;
					byte[] buffer = new byte[1024];
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}

					fos.close();
					ze = zis.getNextEntry();
				}
			} else {
				SM_Utilities.log(dateStr + " - File Not Found !!");
				zipFileFound = false;
			}
			zis.closeEntry();
			zis.close();
			zipFile.delete();
			inStr.close();

		} catch (IOException ex) {
			SM_Utilities.logConsole("Read_DailyBhavCopy_FromBSE | downloadZipFileFromBse | Exception: " + ex);
			ex.printStackTrace();
		}
		return zipFileFound;
	}
}