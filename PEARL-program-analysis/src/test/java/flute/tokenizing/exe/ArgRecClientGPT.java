package flute.tokenizing.exe;

import flute.config.Config;
import flute.tokenizing.excode_data.MultipleArgRecTest;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ArgRecClientGPT extends ArgRecClient {
    public ArgRecClientGPT(String projectName) {
        super(projectName);
    }

    @Override
    void createNewGenerator() {
        generator = new StaticArgRecTestGeneratorGPT(Config.PROJECT_DIR, projectParser);
    }

    public static void main(String[] args) throws IOException {
        RecClient client = new ArgRecClientGPT("lucene");
        List<MultipleArgRecTest> tests = (List<MultipleArgRecTest>) client.getTestsAndReport(false, true);
        //List<MultipleArgRecTest> tests = (List<MultipleArgRecTest>) client.generateTestsFromFile("lucene\\lucene\\src\\java\\org\\apache\\lucene\\search\\QueryTermVector.java");

        client.validateTests(tests, false);
        //RecClient.logTests(tests);
        Collections.shuffle(tests);
        client.queryAndTest(tests, false, false);
        client.printTestResult();
        System.exit(0);
    }
}
