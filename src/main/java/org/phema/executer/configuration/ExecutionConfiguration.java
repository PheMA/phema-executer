package org.phema.executer.configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigOrigin;
import org.phema.executer.exception.PhemaUserException;
import org.phema.executer.interfaces.IExecutionConfiguration;
import org.phema.executer.interfaces.IValueSetRepository;
import org.phema.executer.models.DescriptiveResult;
import org.phema.executer.models.ExecutionModeType;
import org.phema.executer.models.ExecutionReturnType;
import org.phema.executer.valueSets.FileValueSetRepository;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Luke Rasmussen on 8/18/17.
 */
public class ExecutionConfiguration implements IExecutionConfiguration {
    private boolean trustAllSsl = false;
    private ExecutionReturnType returnType = ExecutionReturnType.COUNTS;
    private ExecutionModeType mode = ExecutionModeType.NORMAL;
    private ArrayList<IValueSetRepository> valueSetRepositories;
    private String phenotypeDefinitionFile = "";
    private String executionEngineName = "";
    private String configBaseDirectory = "";
    private Config config = null;

    public ExecutionConfiguration() {
    }

    public ExecutionReturnType getReturnType() {
        return returnType;
    }

    public void setReturnType(ExecutionReturnType returnType) {
        this.returnType = returnType;
    }

    public ExecutionModeType getMode() {
        return mode;
    }

    public void setMode(ExecutionModeType mode) {
        this.mode = mode;
    }

    public String getExecutionEngineName() {
        return executionEngineName;
    }

    public void setExecutionEngineName(String executionEngineName) {
        this.executionEngineName = executionEngineName;
    }

    public void setValueSetRepositories(ArrayList<IValueSetRepository> valueSetRepositories) { this.valueSetRepositories = valueSetRepositories; }

    public String getConfigBaseDirectory() {
        return configBaseDirectory;
    }

    public void setConfigBaseDirectory(String configBaseDirectory) {
        this.configBaseDirectory = configBaseDirectory;
    }

    public boolean isTrustAllSsl() {
        return trustAllSsl;
    }

    public void setTrustAllSsl(boolean trustAllSsl) {
        this.trustAllSsl = trustAllSsl;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public String getPhenotypeDefinitionFile() {
        return phenotypeDefinitionFile;
    }

    public void setPhenotypeDefinitionFile(String phenotypeDefinitionFile) {
        this.phenotypeDefinitionFile = phenotypeDefinitionFile;
    }

    @Override
    public DescriptiveResult validate() {
        return null;
    }

    @Override
    public ArrayList<IValueSetRepository> getValueSetRepositories() {
        return valueSetRepositories;
    }

    @Override
    public DescriptiveResult loadFromConfiguration(Config config) {
        setConfig(config);
        ConfigOrigin origin = config.origin();
        URL configFilePath = origin.url();
        File configFile = new File(configFilePath.getFile());
        setConfigBaseDirectory(configFile.getParent());

        if (config.hasPath("execution.executionMode")) {
            setMode(ExecutionModeType.fromString(config.getString("execution.executionMode")));
        }

        setTrustAllSsl(config.hasPath("execution.trustAllSsl") ? config.getBoolean("execution.trustAllSsl") : false);
        try {
            setValueSetRepositories(processValueSets(config));
        } catch (PhemaUserException pue) {
            return new DescriptiveResult(false, pue.getMessage(), pue);
        } catch (Exception e) {
            // For all other types of exceptions, wrap it in a more user-friendly container.
            return new DescriptiveResult(false, "There was an error when trying to load the configuration details for your value set repositories.  Please double-check that you have specified all of the necessary configuration information, and that the file is formatted correctly.", e);
        }

        setPhenotypeDefinitionFile(config.hasPath("execution.phenotypeDefinition") ? config.getString("execution.phenotypeDefinition") : "");

        setExecutionEngineName(config.hasPath("execution.engine") ? config.getString("execution.engine") : "");

        return validate();
    }

    /**
     * Create all configured value sets for the phenotype definition, given a configuration object
     * @param config The Config object that contains all of the value set definitions
     * @return ArrayList&lt;IValueSetRepository&gt; containing all configured value sets
     * @throws Exception
     */
    private ArrayList<IValueSetRepository> processValueSets(Config config) throws Exception {
        ArrayList<IValueSetRepository> valueSetRepositories = new ArrayList<>();

        List<? extends ConfigObject> valueSetObjects = config.getObjectList("execution.valueSets");
        if (valueSetObjects == null || valueSetObjects.size() == 0) {
            throw new PhemaUserException("You must specify at least one value set for the phenotype to execute.");
        }

        for (ConfigObject valueSetObject : valueSetObjects) {
            if (!valueSetObject.containsKey("type")) {
                throw new PhemaUserException("A value set definition must contain a 'type' field of 'File' or 'CTS2'");
            }

            String valueSetType = valueSetObject.get("type").unwrapped().toString();
            switch (valueSetType) {
                case "File":
                    IValueSetRepository repository = createFileValueSetRepository(valueSetObject);
                    valueSetRepositories.add(repository);
                    break;
                default:
                    throw new PhemaUserException(String.format("A value set type of '%s' is not currently supported.", valueSetType));
            }
        }

        return valueSetRepositories;
    }

    /**
     * Creates a FileValueSetRepository for a given configuration object
     * @param config ConfigObject containing all information needed to create the FileValueSetRepository
     * @return FileValueSetRepository created from the ConfigObject
     * @throws Exception
     */
    private FileValueSetRepository createFileValueSetRepository(ConfigObject config) throws Exception {
        if (!config.containsKey("format")) {
            throw new Exception("A value set definition must contain a 'format' field of 'CSV' or 'VSAC'");
        }

        if (!config.containsKey("path")) {
            throw new Exception("A value set definition must contain a 'path' field");
        }

        String path = config.get("path").unwrapped().toString();
        //updateActionStart(String.format("Creating a value set repository from the file '%s'", path));

        String fullFilePath = getFilePathRelativeToConfigFile(config, "path");
        //updateActionDetails(String.format("Repository path set to '%s'", fullFilePath));

        FileValueSetRepository repository = new FileValueSetRepository();
        repository.initialize(new HashMap<String, String>(){{ put(FileValueSetRepository.Parameters.FilePath,
                fullFilePath); }});
        //updateActionEnd("Created the value set file repository");
        return repository;
    }

    /**
     * Utility function to get a fully qualified file path in relationship to the location of the configuration
     * file.
     * @param fileNameKey
     * @return String
     */
    public static String getFilePathRelativeToConfigFile(ConfigObject config, String fileNameKey) throws PhemaUserException {
        ConfigOrigin origin = config.origin();
        URL configFilePath = origin.url();
        File configFile = new File(configFilePath.getFile());

        if (!config.containsKey(fileNameKey)) {
            throw new PhemaUserException("You are missing the configuration entry '" + fileNameKey + "' in your conf file");
        }

        String path = config.get(fileNameKey).unwrapped().toString();
        String fullFilePath = (new File(configFile.getParent(), path)).getAbsolutePath();
        return fullFilePath;
    }

    /**
     * Expand the relative file given to us for the phenotype definition to the full absolute path where
     * the file lives (relative to the configuration file).
     * @return The absolute path to the phenotype definition file.
     */
    public String getPhenotypeDefinitionPath() {
        if (!config.hasPath("execution.phenotypeDefinition")) {
            return "";
        }

        String path = config.getString("execution.phenotypeDefinition");
        String fullFilePath = (new File(configBaseDirectory, path)).getAbsolutePath();
        return fullFilePath;
    }
}
