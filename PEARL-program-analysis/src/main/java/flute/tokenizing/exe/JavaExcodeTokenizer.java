package flute.tokenizing.exe;

import flute.analysis.config.Config;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import flute.tokenizing.excode_data.NodeSequenceInfo;
import flute.tokenizing.excode_data.SystemTableCrossProject;
import flute.tokenizing.parsing.JavaFileParser;
import flute.tokenizing.visitors.MetricsVisitor;
import flute.utils.file_processing.DirProcessor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JavaExcodeTokenizer {
    private File project;

    public JavaExcodeTokenizer(String projectPath) {
        project = new File(projectPath);
        if (!project.exists()) throw new IllegalArgumentException("Project does not exist!");
        if (!project.isDirectory()) throw new IllegalArgumentException("Not a directory!");
        if (project.isHidden()) throw new IllegalArgumentException("Project is hidden!");
        configure();
    }

    public File getProject() {
        return project;
    }

    private void configure() {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
        StaticJavaParser.getConfiguration().setAttributeComments(false);
        combinedTypeSolver.add(new JavaParserTypeSolver(project));
    }

    public ArrayList<NodeSequenceInfo> tokenize(File javaFile) {
        MetricsVisitor visitor = new MetricsVisitor();
        SystemTableCrossProject systemTableCrossProject = new SystemTableCrossProject();
        JavaFileParser.visitFile(visitor, javaFile, systemTableCrossProject, project.getAbsolutePath());
        systemTableCrossProject.getTypeVarNodeSequence();
        if (systemTableCrossProject.fileList.isEmpty()) return new ArrayList<>();
        else return systemTableCrossProject.fileList.get(0).nodeSequenceList;
    }

    public ArrayList<NodeSequenceInfo> tokenize(String javaFilePath) {
        File javaFile = new File(javaFilePath);
        validateJavaFile(javaFile);
        return tokenize(javaFile);
    }

    public void tokenizeToFile(File javaFile, String outputFilePath) {
        ArrayList<NodeSequenceInfo> nodeSequenceList = tokenize(javaFile);
        File output = new File(outputFilePath);
        try {
            FileWriter fileWriter = new FileWriter(output, false);
            for (NodeSequenceInfo nodeSequence: nodeSequenceList) {
                fileWriter.append(nodeSequence.toStringSimple() + "\r\n");
                fileWriter.flush();
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tokenizeToFile(String javaFilePath, String outputFilePath) {
        File javaFile = new File(javaFilePath);
        validateJavaFile(javaFile);
        tokenizeToFile(javaFile, outputFilePath);
    }

    public List<List<NodeSequenceInfo>> tokenizeProject() {
        List<List<NodeSequenceInfo>> projectExcodes = new ArrayList<>();
        List<File> javaFiles = DirProcessor.walkJavaFile(project.getAbsolutePath());
        for (File file: javaFiles) projectExcodes.add(tokenize(file));
        return projectExcodes;
    }

    public void tokenizeProjectToFile(String outputFilePath) {
        List<File> javaFiles = DirProcessor.walkJavaFile(project.getAbsolutePath());
        File output = new File(outputFilePath);
        for (int i = 0; i < javaFiles.size(); ++i) {
            File file = javaFiles.get(i);
            ArrayList<NodeSequenceInfo> nodeSequenceList = tokenize(file);
            try {
                FileWriter fileWriter = new FileWriter(output, i > 0);
                fileWriter.append(file.getAbsolutePath() + "\r\n");
                for (NodeSequenceInfo nodeSequence: nodeSequenceList) {
                    fileWriter.append(nodeSequence.toStringSimple() + "\r\n");
                    fileWriter.flush();
                }
                fileWriter.append("\r\n");
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized static void validateJavaFile(File javaFile) {
        if (!javaFile.exists()) throw new IllegalArgumentException("File does not exist!");
        if (!(javaFile.isFile() && javaFile.getName().toLowerCase().endsWith(".java"))) throw new IllegalArgumentException("Not a java file!");
        if (javaFile.isHidden()) throw new IllegalArgumentException("File is hidden!");
    }

    public synchronized static void main(String[] args) {
        JavaExcodeTokenizer tokenizer = new JavaExcodeTokenizer(Config.REPO_DIR + "sampleproj/");
        tokenizer.tokenizeToFile(Config.REPO_DIR + "sampleproj/src/Main.java", Config.LOG_DIR + "debugTokenizer.txt");

        //JavaExcodeTokenizer tokenizer = new JavaExcodeTokenizer(Config.REPO_DIR + "git/lucene/");
        //tokenizer.tokenizeProjectToFile(Config.LOG_DIR + "debugTokenizer.txt");
    }
}
