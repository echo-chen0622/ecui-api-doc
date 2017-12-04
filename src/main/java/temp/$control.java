package temp;

import java.util.List;
import java.util.Map;
import org.lilystudio.smarty4j.Context;
import org.lilystudio.smarty4j.statement.AbstractCustomModifier;

public class $control
        extends AbstractCustomModifier
{
  @Override
  protected Object execute(Context context, Object o, Object[] values)
  {
    List<Map<String, Object>> list = (List)context.get("control");
    String name = (String)o;
    for (Map<String, Object> item : list) {
      if (item.get("name").equals(name))
      {
        String path = (String)context.get("PATH");
        return "<a href=\"" + (
                "/control/".equals(path) ? "" : "/".equals(path) ? "control/" :
                        "../control/") + item.get("fileName") + ".html\">" + name +
                "</a>";
      }
    }

    return null;
  }
}
