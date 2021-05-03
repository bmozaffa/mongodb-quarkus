package org.acme.mongodb.servicebinding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import io.quarkus.kubernetes.service.binding.runtime.ServiceBinding;
import io.quarkus.kubernetes.service.binding.runtime.ServiceBindingConfigSource;
import io.quarkus.kubernetes.service.binding.runtime.ServiceBindingConverter;

public class MongoServiceBindingConverter implements ServiceBindingConverter {
    private static final Logger LOGGER = Logger.getLogger(MongoServiceBindingConverter.class.getName());

    @Override
    public Optional<ServiceBindingConfigSource> convert(List<ServiceBinding> serviceBindings) {
        String bindingType = "MongoDB";
        Optional<ServiceBinding> matchingByType = ServiceBinding.singleMatchingByType(bindingType, serviceBindings);
        if (!matchingByType.isPresent()) {
            LOGGER.info("Did not find a MongoDB type binding");
            return Optional.empty();
        }

        Map<String, String> properties = new HashMap<>();
        ServiceBinding binding = matchingByType.get();

        Map<String, String> bindings = binding.getProperties();
        String user = bindings.get("db.user");
        String password = bindings.get("db.password");
        String host = bindings.getOrDefault("db.host", "localhost");
        String port = bindings.get("db.port");
        String hostPort;
        if( port == null ) {
            hostPort = host;
        } else {
            hostPort = String.format("%s:%s", host, port);
        }
        String database = bindings.getOrDefault("db.name", "");
        String mongoConnectionString;
        if( user == null ) {
            mongoConnectionString = String.format("mongodb+srv://%s/%s?retryWrites=true&w=majority", hostPort, database);
        } else {
            mongoConnectionString = String.format("mongodb+srv://%s:%s@%s/%s?retryWrites=true&w=majority", user, password, hostPort, database);
        }
        LOGGER.info("MongoDB Connection String is " + mongoConnectionString);
        properties.put("MONGODB_CONNECTION_STRING", mongoConnectionString);
        return Optional.of(new ServiceBindingConfigSource(bindingType + "-k8s-service-binding-source", properties));
    }
}
