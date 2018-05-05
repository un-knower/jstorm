package com.hollyvoc.helper.jdbc;


import com.hollycrm.util.config.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;


/**
 * Created by qianxm on 2017/7/27.
 *
 */
public class JdbcHelper {

    private static Logger logger = LoggerFactory.getLogger(JdbcHelper.class);
    private static JdbcHelper instance = new JdbcHelper();
//    static {
//        try {
//            Class.forName(ConfigUtils.getStrVal("jdbc.driver"));
//        }catch (ClassNotFoundException e){
//            logger.error("can`t find oracle driver class", e);
//        }
//    }

    private JdbcHelper(){
//        logger.info(" JdbcHelper ");
        try {
            logger.info("driver: " + ConfigUtils.getStrVal("jdbc.driver"));
            Class.forName(ConfigUtils.getStrVal("jdbc.driver"));
        }catch (ClassNotFoundException e){
            logger.error("can`t find oracle driver class", e);
        }

        try {
            this.connection = DriverManager.getConnection(
                    ConfigUtils.getStrVal("jdbc.url"),
                    ConfigUtils.getStrVal("jdbc.userName"),
                    ConfigUtils.getStrVal("jdbc.password"));
            String url = ConfigUtils.getStrVal("jdbc.url");
//            System.out.println(" url " + url);
//            logger.info(" url " + url);
        }catch (SQLException e){
            logger.error("connect db failure!", e);
        }
    }
    public static JdbcHelper getInstance(){
        return instance;
    }

    private Connection connection;

    public int insert(String sql, String ... params) throws SQLException{
        try(PreparedStatement ps = connection.prepareStatement(sql)){
            for(int i = 0; i< params.length; i ++){
                ps.setString(i+1, params[i]);
            }
            return ps.executeUpdate();

        }
    }

    /**
     * 批量提交.
     * @param sql sql
     * @param params 需要提价的参数
     * @return 返回结果
     * @throws SQLException 提交异常
     */
    public boolean batchInsert(String sql, List<String[]> params) throws SQLException {
        boolean result = false;
        try(PreparedStatement ps = connection.prepareStatement(sql)){
            this.connection.setAutoCommit(false); // 关闭自动提交事务
            for(int i = 0; i< params.size(); i ++){
                String[] o = params.get(i);
//                System.out.println(" o" + o.length);
                for(int j=0; j<o.length;j++){
                    ps.setString(j+1, o[j]);
                }
                ps.addBatch();
            }

            ps.executeBatch();
            connection.commit();
            result = true;
        } catch (Exception e) {
            // 提交失败回滚
            connection.rollback();
//            logger.error(" batch commit error!",e);
            System.out.println(" batch commit error!" + e);
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取数据
     * @param sql sql
     * @param params 参数
     * @return 返回结果
     * @throws SQLException 可能出现的查询异常
     */
    public ResultSet get(String sql, String... params) throws SQLException {
        try(PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }
            return ps.executeQuery();
        }
    }





}
