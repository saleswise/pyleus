package com.yelp.pyleus;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yelp.pyleus.bolt.PythonBolt;
import com.yelp.pyleus.spout.PythonSpout;

public class PythonComponentsFactory {

    public static final String VIRTUALENV_INTERPRETER = "pyleus_venv/bin/python";
    public static final String MODULE_OPTION = "-m";

    private String[] buildCommand(final String baseExec, final Map<String, Object> argumentsMap,
        final String loggingConfig, final String serializerConfig) {

        String[] command = new String[3];

        command[0] = "bash";
        command[1] = "-c";

        StringBuilder strBuf = new StringBuilder(baseExec);

        if (argumentsMap != null) {
            Gson gson = new GsonBuilder().create();
            String json = gson.toJson(argumentsMap);
            json = json.replace("\"", "\\\"");
            strBuf.append(String.format(" --options \"%s\"", json));
        }

        {
            Map<String, Object> pyleusConfig = new HashMap<String, Object>();
            pyleusConfig.put("logging_config_path", loggingConfig);
            pyleusConfig.put("serializer", serializerConfig);
            Gson gson = new GsonBuilder().create();
            String json = gson.toJson(pyleusConfig);
            json = json.replace("\"", "\\\"");
            strBuf.append(String.format(" --pyleus-config \"%s\"", json));
        }

        command[2] = strBuf.toString();

        return command;
    }

    private String buildBaseExec(final String module) {

        // Done before launching any spout or bolt in order to cope with Storm permissions bug
        StringBuilder strBuf = new StringBuilder();
        strBuf.append(String.format("chmod 755 %s; %s", VIRTUALENV_INTERPRETER, VIRTUALENV_INTERPRETER));
        strBuf.append(String.format(" %s %s", MODULE_OPTION, module));
        return strBuf.toString();
    }

    public PythonBolt createPythonBolt(final String module, final Map<String, Object> argumentsMap,
        final String loggingConfig, final String serializerConfig) {

        return new PythonBolt(buildCommand(buildBaseExec(module), argumentsMap, loggingConfig, serializerConfig));
    }

    public PythonBolt createGoBolt(final String binary, final Map<String, Object> argumentsMap,
                                   final String loggingConfig, final String serializerConfig) {

        return new PythonBolt(buildCommand(binary, argumentsMap, loggingConfig, serializerConfig));
    }

    public PythonSpout createPythonSpout(final String module, final Map<String, Object> argumentsMap,
        final String loggingConfig, final String serializerConfig) {

        return new PythonSpout(buildCommand(buildBaseExec(module), argumentsMap, loggingConfig, serializerConfig));
    }
}
