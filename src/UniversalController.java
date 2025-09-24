import Bind.Bind;

import javax.script.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public class UniversalController {

    private Map<String, List<Double>> dataMap = new HashMap<>();
    private Map<String, Object> scriptvar = new HashMap<>();

    private int LL;

    public void setdata(String filePath) {

        try {
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                String name = parts[0];
                List<Double> values = new ArrayList<>();
                for (int i = 1; i < parts.length; i++) {
                    values.add(Double.parseDouble(parts[i]));
                }
                dataMap.put(name, values);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void setmodel(Object model) {
        scriptvar.clear();
        Class<?> modelClass = model.getClass();
        LL = dataMap.get("LATA").size();

        Arrays.stream(modelClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Bind.class))
                .forEach(field -> {
                    String fieldName = field.getName();
                    List<Double> values = dataMap.get(fieldName);

                    try {
                        field.setAccessible(true);
                        if (fieldName.equals("LL")) {
                            field.set(model, LL);
                        } else if (field.getType().isArray()) {
                            double[] arr = new double[LL];
                            if (values != null) {
                                for (int i = 0; i < LL; i++) {
                                    arr[i] = i < values.size() ? values.get(i) : values.get(values.size() - 1);
                                }
                            }
                            field.set(model, arr);

                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException();
                    }
                });

    }


    public void runModel(Object model) {
        try {

            Method run = model.getClass().getMethod("run");
            run.invoke(model);
            Arrays.stream(model.getClass().getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(Bind.class))
                    .forEach(field -> {
                        try {
                            field.setAccessible(true);
                            Object value = field.get(model);
                            scriptvar.put(field.getName(), value);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void runScript(String scriptCode) {
        ScriptEngine groovy = new ScriptEngineManager().getEngineByName("groovy");
        scriptvar.forEach((key, value) -> {
            groovy.put(key, value);
        });

        try {
            groovy.eval(scriptCode);
            for (String key : scriptvar.keySet()) {
                Object updatedValue = groovy.get(key);
                scriptvar.put(key, updatedValue);
            }

            for (Object key : groovy.getBindings(ScriptContext.ENGINE_SCOPE).keySet()) {
                if (key instanceof String && !scriptvar.containsKey(key)) {
                    scriptvar.put((String) key, groovy.get((String) key));
                }
            }
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }

    public void filescript(String filePath) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        runScript(sb.toString());
    }

    public String resulttable(Object model) {
        StringBuilder sb = new StringBuilder();

        List<Double> lata = dataMap.get("LATA");
        int len = lata.size();

        sb.append("LATA");
        for (double d : lata) {
            sb.append("\t").append((int) d);
        }
        sb.append("\n");

        Field[] modelFields = Arrays.stream(model.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Bind.class))
                .filter(field -> !field.getName().equals("LL"))
                .toArray(size -> new Field[size]);

        for (Field field : modelFields) {
            try {
                field.setAccessible(true);
                String fieldName = field.getName();
                Object value = field.get(model);

                sb.append(fieldName);

                if (value instanceof double[]) {
                    double[] arr = (double[]) value;
                    for (int i = 0; i < len; i++) {
                        double val = (i < arr.length) ? arr[i] : 0.0;
                        sb.append("\t").append(val);
                    }
                } else {
                    String sVal = value != null ? value.toString() : "0.0";
                    for (int i = 0; i < len; i++) {
                        sb.append("\t").append(sVal);
                    }
                }
                sb.append("\n");
            } catch (IllegalAccessException e) {
                throw new RuntimeException();
            }
        }

        for (Map.Entry<String, Object> entry : scriptvar.entrySet()) {
            String varName = entry.getKey();
            if (varName.equals("LL")) {
                continue;
            }

            if (varName.length() == 1) {
                continue;
            }


            boolean exists = false;
            for (Field field : modelFields) {
                if (field.getName().equals(varName)) {
                    exists = true;
                    break;
                }
            }
            if (exists) {
                continue;
            }


            sb.append(varName);
            Object value = entry.getValue();

            if (value instanceof double[]) {
                double[] arr = (double[]) value;
                for (int i = 0; i < len; i++) {
                    double val = (i < arr.length) ? arr[i] : 0.0;
                    sb.append("\t").append(val);
                }
            } else {
                String sVal = value != null ? value.toString() : "0.0";
                sb.append("\t").append(sVal);
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
