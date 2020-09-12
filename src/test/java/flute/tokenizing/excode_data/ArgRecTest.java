package flute.tokenizing.excode_data;

import java.util.List;

public class ArgRecTest {
    private List<String> lex_context;
    private String excode_context;

    private List<String> next_lex;
    private List<String> next_excode;

    private String expected_lex;
    private String expected_excode;

    public List<String> getLex_context() {
        return lex_context;
    }

    public void setLex_context(List<String> lex_context) {
        this.lex_context = lex_context;
    }

    public String getExcode_context() {
        return excode_context;
    }

    public void setExcode_context(String excode_context) {
        this.excode_context = excode_context;
    }

    public List<String> getNext_lex() {
        return next_lex;
    }

    public void setNext_lex(List<String> next_lex) {
        this.next_lex = next_lex;
    }

    public List<String> getNext_excode() {
        return next_excode;
    }

    public void setNext_excode(List<String> next_excode) {
        this.next_excode = next_excode;
    }

    public String getExpected_lex() {
        return expected_lex;
    }

    public void setExpected_lex(String expected_lex) {
        this.expected_lex = expected_lex;
    }

    public String getExpected_excode() {
        return expected_excode;
    }

    public void setExpected_excode(String expected_excode) {
        this.expected_excode = expected_excode;
    }
}