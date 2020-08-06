package com.se.entity;

import java.util.HashSet;
import java.util.Set;

/**
 * @author wangjiwen
 * @create 2020/8/3
 */
public class SetEnum {
    public Set<String> controlEnum = new HashSet<>();
    public Set<String> serviceEnum = new HashSet<>();
    public Set<String> daoEnum = new HashSet<>();
    public Set<String> otherEnum = new HashSet<>();

    public SetEnum() {
        controlEnum.add("Controller");
        controlEnum.add("RestController");
        controlEnum.add("RequestMapping");
        controlEnum.add("GetMapping");
        controlEnum.add("PostMapping");
        controlEnum.add("PutMapping");
        controlEnum.add("DeleteMapping");
        controlEnum.add("PatchMapping");
        controlEnum.add("PathVariable");
        controlEnum.add("RequestParam");
        controlEnum.add("RequestBody");
        controlEnum.add("ResponseBody");

        serviceEnum.add("Service");

        daoEnum.add("Repository");
        daoEnum.add("Table");
        daoEnum.add("ID");
        daoEnum.add("column");
        daoEnum.add("Query");
        daoEnum.add("Transactional");
        daoEnum.add("Modifying");
        daoEnum.add("GeneratedValue");
        daoEnum.add("GenericGenerator");
        daoEnum.add("Transient");
        daoEnum.add("Lob");
        daoEnum.add("Enumerated");
        daoEnum.add("OneToOne");
        daoEnum.add("OneToMany");
        daoEnum.add("ManyToOne");
        daoEnum.add("ManyToMany");

        otherEnum.add("SpringBootApplication");
        otherEnum.add("Configuration");
        otherEnum.add("EnableAutoConfiguration");
        otherEnum.add("ComponentScan");
        otherEnum.add("Autowired");
        otherEnum.add("value");
        otherEnum.add("ConfigurationProperties");
        otherEnum.add("PropertySource");
    }
}
