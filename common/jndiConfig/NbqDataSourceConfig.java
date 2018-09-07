package jp.co.pmacmobile.common.jndiConfig;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;

/**
 * データソースNBQコンフィグ
 *
 * @author hitachi
 *
 */
@Configuration
@MapperScan(basePackages = "jp.co.pmacmobile.domain.mapper.nbq", sqlSessionTemplateRef = "nbqSqlSessionTemplate")
public class NbqDataSourceConfig {

    @Bean(name = "nbqDataSource")
    public DataSource dataSource() {
        JndiDataSourceLookup dataSourceLookup = new JndiDataSourceLookup();
        DataSource dataSource = dataSourceLookup.getDataSource("jdbc/DSPMAC");
        return dataSource;
    }

    @Bean(name = "nbqTransactionManager")
    public DataSourceTransactionManager setTransactionManager(@Qualifier("nbqDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "nbqSqlSessionFactory")
    public SqlSessionFactory setSqlSessionFactory(@Qualifier("nbqDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver()
                        .getResources("classpath:jp/co/pmacmobile/domain/mapper/nbq/*.xml"));
        return bean.getObject();
    }

    @Bean(name = "nbqSqlSessionTemplate")
    public SqlSessionTemplate setSqlSessionTemplate(
                    @Qualifier("nbqSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
