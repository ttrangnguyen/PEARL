package flute.data.testcase;

import com.github.javaparser.Position;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BaseTestCase {
    protected String projectName;
    protected String relativeFilePath;
    protected String type = "UNKNOWN";
    protected Position beginPosition;

    protected String context;
    protected String outerMethodSignature;

    protected String target;

    public BaseTestCase(String projectName, String relativeFilePath, Position beginPosition, String context, String outerMethodSignature, String target) {
        this.projectName = projectName;
        this.relativeFilePath = relativeFilePath;
        this.beginPosition = beginPosition;
        this.context = context;
        this.outerMethodSignature = outerMethodSignature;
        this.target = target;
    }

    public BaseTestCase(BaseTestCase other) {
        this(other.projectName, other.relativeFilePath, other.beginPosition, other.context, other.outerMethodSignature, other.target);
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {

        return "------\n" +
                "BaseTestCase{" +
                "projectName='" + projectName + '\'' +
                ", relativeFilePath='" + relativeFilePath + '\'' +
                ", type='" + this.getType() + '\'' +
                ", beginPosition=" + beginPosition +
                ", context='" + context + '\'' +
                ", outerMethodSignature='" + outerMethodSignature + '\'' +
                ", target='" + target + '\'' +
                '}';
    }

    public String dumpToJson(){
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.toJson(this);
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getRelativeFilePath() {
        return relativeFilePath;
    }

    public void setRelativeFilePath(String relativeFilePath) {
        this.relativeFilePath = relativeFilePath;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Position getBeginPosition() {
        return beginPosition;
    }

    public void setBeginPosition(Position beginPosition) {
        this.beginPosition = beginPosition;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getOuterMethodSignature() {
        return outerMethodSignature;
    }

    public void setOuterMethodSignature(String outerMethodSignature) {
        this.outerMethodSignature = outerMethodSignature;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}