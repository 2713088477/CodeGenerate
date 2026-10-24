package com.hao.backend.generator;

import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

public class MybatisFlexCodegen {

    private static final String BASE_PACKAGE = "com.hao.backend.codegen";
    private static final String[] TABLES = {"user"};

    public static void main(String[] args) throws IOException {
        //读取yaml的配置文件,获取到数据库的连接信息
        PropertySource<?> source = new YamlPropertySourceLoader()
                .load("application", new ClassPathResource("application.yaml"))
                .get(0);

        //配置数据源
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(String.valueOf(source.getProperty("spring.datasource.url")));
        dataSource.setUsername(String.valueOf(source.getProperty("spring.datasource.username")));
        dataSource.setPassword(String.valueOf(source.getProperty("spring.datasource.password")));

        GlobalConfig globalConfig = createGlobalConfigUseStyle();

        //通过 datasource 和 globalConfig 创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig);

        //生成代码
        generator.generate();
    }

    public static GlobalConfig createGlobalConfigUseStyle() {
        //创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();

        //设置根包
        globalConfig.getPackageConfig()
                .setBasePackage(BASE_PACKAGE);

        //设置表前缀和只生成哪些表，setGenerateTable 未配置时，生成所有表
        globalConfig.getStrategyConfig()
                .setGenerateTable(TABLES);

        //设置逻辑删除列
        globalConfig.setLogicDeleteColumn("isDeleted");

        //设置生成 entity 并启用 Lombok
        globalConfig.enableEntity()
                .setWithLombok(true)
                .setJdkVersion(21);

        //设置生成 mapper
        globalConfig.enableMapper();
        globalConfig.enableMapperXml();

        //设置service
        globalConfig.enableService();
        globalConfig.enableServiceImpl();

        //设置controller
        globalConfig.enableController();

        //设置注释信息
        globalConfig.getJavadocConfig()
            .setAuthor("<a href=\"https://github.com/2713088477\">艺艺生辉</a>")
            .setSince("");
            
        return globalConfig;
    }
}