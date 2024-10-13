package cn.devmeteor.ruanzhucode;


import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;


public class App {

    private static final String name = "软件著作权代码文档生成器";
    private static final String version = "v1.0.0";
    private static final String sourcePath = "E:\\project\\RuanZhuCode\\code";
    private static final String outputPath = "E:\\project\\RuanZhuCode\\output";
    private static final String[] myExcludeFiles = new String[]{"E:\\project\\RuanZhuCode\\RuanZhuCode.iml","E:\\project\\RuanZhuCode\\README.md"};
    private static final String[] myExcludeDirs = new String[]{"E:\\project\\RuanZhuCode\\target","E:\\project\\RuanZhuCode\\.idea","E:\\project\\RuanZhuCode\\src\\test"};
    private static final String[] additionalFiles = new String[]{};
    private static final String[] additionalDirs = new String[]{};


    public static void main(String[] args) throws IOException {
        List<String> excludeDirs = Arrays.asList(myExcludeDirs);
        String[] audios = new String[]{"mp3", "wav", "aif", "aiff", "mp1", "mp2", "ra", "ram", "mid", "rmi", "m4a", "wma", "cda", "ogg", "ape", "flac", "aac", "ac3", "mmf", "amr", "m4r", "wavpack"};
        String[] videos = new String[]{"avi", "mov", "qt", "asf", "rm", "rmvb", "navi", "divx", ",mp4", "mpeg", "mpg", "flv", "mkv", "3gp", "wmv", "vob", "swf"};
        String[] images = new String[]{"webp", "jpg", "png", "ico", "bmp", "gif", "tif", "tga", "pcx", "jpeg", "exif", "fpx", "svg", "psd", "cdr", "pcd", "dxf", "ufo", "eps", "ai", "hdri", "raw", "wmf", "flic", "emf"};
        String[] docs = new String[]{"doc", "docx", "xls", "ppt", "pptx", "pdf"};
        String[] executable = new String[]{"exe", "apk", "ipa", "app"};
        String[] zips = new String[]{"zip", "rar", "arj", "z", "tar", "gz", "iso", "jar"};
        List<String> excludeFiles = new ArrayList<>();
        excludeFiles.addAll(Arrays.asList(audios));
        excludeFiles.addAll(Arrays.asList(videos));
        excludeFiles.addAll(Arrays.asList(images));
        excludeFiles.addAll(Arrays.asList(docs));
        excludeFiles.addAll(Arrays.asList(executable));
        excludeFiles.addAll(Arrays.asList(zips));
        excludeFiles.addAll(Arrays.asList(myExcludeFiles));
        File root = new File(sourcePath);
        Queue<File> dirQueue = new ArrayDeque<>();
        dirQueue.add(root);
        List<File> files = new ArrayList<>();
        while (!dirQueue.isEmpty()) {
            File dir = dirQueue.poll();
            for (File f : dir.listFiles()) {
                if (f.isDirectory() && !excludeDirs.contains(f.getAbsolutePath()) && !f.getName().equals(".git"))
                    dirQueue.add(f);
                else if (f.isFile() && !matchExclude(f, excludeFiles))
                    files.add(f);
            }
        }
        for (String additional : additionalFiles)
            files.add(new File(additional));

        // 新的字符串构建器，用于有效管理内存
        StringBuilder stringBuilder = new StringBuilder();

        for (File file : files) {
            try (Scanner scanner = new Scanner(new FileInputStream(file), "UTF-8")) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();

                    // 删除单行注释
                    line = line.replaceAll("//.*", "");

                    // 逐行处理，避免加载整个文件
                    stringBuilder.append(line).append("\n");
                }
            }
        }

        // 将StringBuilder转换为String
        String s = stringBuilder.toString();

        // 删除多行注释
        s = removeBlockComments(s);

        // 删除XML注释
        s = s.replaceAll("<!--.*?-->", "");

        // 删除空行
        s = s.replaceAll("(?m)^[ \t]*\r?\n", "");
        XWPFDocument doc = new XWPFDocument(new FileInputStream("template/template.docx"));
        List<XWPFRun> runs = doc.getHeaderList().get(1).getParagraphs().get(1).getRuns();
        runs.get(0).setText(name,0);
        runs.get(1).setText(version,0);
        Scanner scanner = new Scanner(s);
        int total = 0;
        while (scanner.hasNext()) {
            total++;
            scanner.nextLine();
        }
        System.out.println("总计：" + total + "行");
        scanner = new Scanner(s);
        while (scanner.hasNext()) {
            XWPFParagraph p1 = doc.createParagraph();
            XWPFRun r1 = p1.createRun();
            r1.setFontFamily("等线 (西文正文)");
            r1.setFontSize(10);
            r1.setText(scanner.nextLine());
        }
        scanner.close();
        doc.getDocument().getBody().removeP(0);
        FileOutputStream out = new FileOutputStream(outputPath + "/" + name + version + "源代码.docx");
        doc.getProperties().getCoreProperties().setCreator("软件著作权代码文档生成器");
        doc.getProperties().getCoreProperties().setLastModifiedByUser("软件著作权代码文档生成器");
        doc.getProperties().getCoreProperties().setRevision("1");
        doc.getProperties().getCoreProperties().setModified(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        doc.write(out);
        out.close();
    }
    /**
     * 以有效的方式删除块注释
     * @param text 要处理的文本
     * @return 删除块注释后的文本
     */
    private static String removeBlockComments(String text) {
        StringBuilder newString = new StringBuilder();
        boolean inBlockComment = false;

        Scanner scanner = new Scanner(text);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if (line.contains("/*")) {
                inBlockComment = true;
            }

            if (!inBlockComment) {
                newString.append(line).append("\n");
            }

            if (line.contains("*/")) {
                inBlockComment = false;
            }
        }
        scanner.close();

        return newString.toString();
    }


    private static boolean matchExclude(File f, List<String> excludeList) {
        for (String e : excludeList)
            if (f.getAbsolutePath().equals(e) || f.getName().equals(".gitignore") || f.getName().endsWith("." + e.toLowerCase()))
                return true;
        return false;
    }
}
