package temp;

import com.ruixus.smarty4j.Context;
import com.ruixus.smarty4j.SafeContext;
import com.ruixus.smarty4j.TemplateWriter;
import com.ruixus.smarty4j.statement.Definition;
import com.ruixus.smarty4j.statement.LineFunction;

import java.util.List;
import java.util.Map;

public class $method
        extends LineFunction
{

    /** 参数定义 */
    private static final Definition[] definitions = {
            Definition.forFunction("fileName", Definition.Type.OBJECT)};

    @Override
    public Definition[] getDefinitions() {
        return definitions;
    }
    public Object execute(SafeContext ctx, TemplateWriter writer, Object fileName) throws Exception {
        Context context = (Context) ctx;
        List<Map<String, Object>> list = (List<Map<String, Object>>)context.get("methods");
        for (Map<String, Object> item : list) {
            if (item.get("fileName").equals(fileName))
            {
                String path = (String)context.get("PATH");
                String url = "<a href=\"" + (
                        "/method/".equals(path) ? "" : "/".equals(path) ? "method/" :
                                "../method/") + fileName + ".html\">" + item.get("name") +
                        "</a>";
                writer.write(url);
            }
        }
        return null;
    }

}
