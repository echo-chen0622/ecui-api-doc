package com.ecui;

import com.ruixus.smarty4j.Engine;
import com.ruixus.smarty4j.Template;
import com.ruixus.smarty4j.TemplateException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author chentiancheng
 * @date 2017/12/6
 * @time 13:14
 * @describe
 */
public class EngineForEcuiDoc extends Engine {
    private Map<String, Template> tpls = new HashMap<String, Template>();

    @Override
    public Template getTemplate(String name) throws IOException, TemplateException {
        Template tpl = tpls.get(name);
        if (tpl != null && !(tpl.isUpdated())) {
            return tpl;
        }

        tpl = new Template(this, new File(name));
        tpls.put(name, tpl);
        return tpl;
    }
}
