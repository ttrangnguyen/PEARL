package flute.data.typemodel;

import org.eclipse.jdt.core.dom.IMethodBinding;

public class ConstructorMember extends Member{
    public ConstructorMember(IMethodBinding member) {
        super(member);
    }
}
