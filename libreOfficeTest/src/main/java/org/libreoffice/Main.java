package org.libreoffice;

import java.io.File;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
//    public final static String CONTEXT_PATH = "D:\\programming\\github2\\Java-Library-Test\\hwplib_hwpxlib_test\\src\\test\\resources\\sampleHwpFiles\\";
    
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        
        System.out.println("hwpx 파일이 있는 폴더 경로를 적어 주세요. ( default path: /home/alvisapi/sampleHwpxFiles/");
        String contextPath = sc.nextLine().trim();
        if(contextPath.isEmpty()) {
            contextPath = "/home/alvisapi/sampleHwpxFiles/";
        }
        if(!String.valueOf(contextPath.charAt(contextPath.length() - 1)).equals(File.separator)) {
            contextPath += File.separator;
        }
        System.out.println("변환할 파일명을 입력해 주세요. ( 확장자 포함 )");
        String filename = sc.nextLine().trim();
        
        System.out.println("PDF 파일을 저장할 경로를 적어주세요. ( default path: /home/alvisapi/samplePDFResults/");
        String outDir = sc.nextLine().trim();
        if(outDir.isEmpty()) {
            outDir = "/home/alvisapi/samplePDFResults/";
        }
        if(!String.valueOf(outDir.charAt(outDir.length() - 1)).equals(File.separator)) {
            outDir += File.separator;
        }
        
        String excludeExtension = filename.split("\\.")[0];
        LibreOfficeConverter libreOfficeConverter = new LibreOfficeConverter();
        String inputFilePath = contextPath+filename;
        String outputFilePath = outDir+excludeExtension+".pdf";

        System.out.println("filename: "+filename);
        System.out.println("inputFilePath: "+inputFilePath);
        System.out.println("outputFilePath: "+outputFilePath);
        libreOfficeConverter.convertToPdf(inputFilePath, outputFilePath);
        System.exit(0);
    }
}