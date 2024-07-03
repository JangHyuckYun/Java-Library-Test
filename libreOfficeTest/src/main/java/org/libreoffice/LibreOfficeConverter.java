package org.libreoffice;


import com.sun.star.beans.PropertyValue;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import ooo.connector.BootstrapSocketConnector;
import ooo.connector.server.OOoServer;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LibreOfficeConverter {
    
    public static XComponentContext connect() {
        try {
            // LibreOffice에 연결
            String officePath = "D:\\Program Files\\LibreOffice\\program\\soffice.exe";
//            String officePath = "/opt/libreoffice7.6/program/soffice";
            File officeExecutable = new File(officePath);

            if (!officeExecutable.exists()) {
                throw new BootstrapException("LibreOffice 실행 파일을 찾을 수 없습니다: " + officePath);
            }

            // 환경 변수 설정
            System.setProperty("uno.path", officePath);

            List oooOptions = new ArrayList();
            oooOptions.add("--nofirststartwizard");
            oooOptions.add("--infilter=\"Hwp2002_File\"");

            OOoServer oOoServer = new OOoServer(officePath);
            BootstrapSocketConnector bootstrapSocketConnector = new ooo.connector.BootstrapSocketConnector(oOoServer);
            return bootstrapSocketConnector.connect();
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    public boolean existFile(String filePath) {
        try {
            File file = new File(filePath);
            return file.exists();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public PropertyValue createPropertyValue(String key, Object value) {
        PropertyValue propertyValue = new PropertyValue();
        propertyValue.Name = key;
        propertyValue.Value = value;

        return propertyValue; 
    }
    public void convertToPdf(String inputFilePath, String outputFilePath) throws Exception {
        if(!existFile(inputFilePath)) throw new Exception("Cannot find file '"+inputFilePath+"'"); 

        XComponentContext xContext = this.connect();
        XComponentLoader xLoader = UnoRuntime.queryInterface(
                XComponentLoader.class,
                xContext.getServiceManager().createInstanceWithContext(
                        "com.sun.star.frame.Desktop", xContext));

        // 문서 로드
        String fileUrl = new File(inputFilePath).toURI().toString();
        PropertyValue[] loadProps = new PropertyValue[0];

        XComponent document = xLoader.loadComponentFromURL(fileUrl, "_blank", 0, loadProps);
        // PDF로 변환
        PropertyValue[] convertProps = new PropertyValue[1];
        convertProps[0] = createPropertyValue("FilterName", "writer_pdf_Export");

        String outputUrl = new File(outputFilePath).toURI().toString();
        System.out.println("outputUrl:"+outputUrl);
        com.sun.star.frame.XStorable xStorable = UnoRuntime.queryInterface(com.sun.star.frame.XStorable.class, document);
        xStorable.storeToURL(outputUrl, convertProps);
        System.out.println("out finish~~");

        // 문서 닫기
        com.sun.star.util.XCloseable xCloseable = UnoRuntime.queryInterface(com.sun.star.util.XCloseable.class, document);
        System.out.println("xCloseable: "+ (xCloseable == null));
        if (xCloseable != null) {
            xCloseable.close(true);
        } else {
            com.sun.star.lang.XComponent xComp = UnoRuntime.queryInterface(com.sun.star.lang.XComponent.class, document);
            xComp.dispose();
        }
    }
}
