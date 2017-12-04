package temp;

import org.lilystudio.smarty4j.Context;
import org.lilystudio.smarty4j.statement.AbstractCustomModifier;

import java.util.List;
import java.util.Map;

public class $method
  extends AbstractCustomModifier
{
  @Override
  protected Object execute(Context context, Object o, Object[] values)
  {

    List<Map<String, Object>> list = (List<Map<String, Object>>)context.get("method");
    String fileName = (String)o;

    for (Map<String, Object> item : list) {
      if (item.get("fileName").equals(fileName))
      {
        String path = (String)context.get("PATH");
        return "<a href=\"" + (
                "/method/".equals(path) ? "" : "/".equals(path) ? "method/" :
          "../method/") + fileName + ".html\">" + item.get("name") +
          "</a>";
      }
    }
    return null;
  }
}
