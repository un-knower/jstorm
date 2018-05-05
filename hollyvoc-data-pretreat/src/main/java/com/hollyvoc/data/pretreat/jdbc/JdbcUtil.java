package com.hollyvoc.data.pretreat.jdbc;


import com.hollyvoc.data.pretreat.config.Config;

import java.sql.*;

/**
 * Created by alleyz on 2017/5/17.
 *
 */
public class JdbcUtil {

    // 懒汉式实例， 双重检查防止多实例
    private static JdbcUtil instance = null;
    public static JdbcUtil getInstance() {
        if(instance == null) {
            synchronized (JdbcUtil.class) {
                if(instance == null)
                    instance = new JdbcUtil();
            }
        }
        return instance;
    }
    private JdbcUtil() {
        try {
            Class.forName(Config.getVal("jdbc.driver"));
            getConn(); // 获取默认连接
        }catch (ClassNotFoundException e) {
            System.out.println("加载驱动类出错，请检查驱动类是否存在[jdbc.driver]");
        }catch (SQLException e) {
            e.printStackTrace();
            System.out.println("连接数据库出错，请检查数据库连接信息是否有误!jdbcUrl[jdbc.url],userName[jdbc.userName]");
        }
    }
    private Connection conn = null;
    private void getConn() throws SQLException{
        conn =  DriverManager.getConnection(Config.getVal("jdbc.url"), Config.getVal("jdbc.userName"), Config.getVal("jdbc.password"));
    }

    public <T>T query(String sql, Object[] params, ResultSetCallback<T> callback) throws SQLException{
        if(conn == null) {
            getConn();
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        T t;
        try {
            ps = conn.prepareStatement(sql);
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; ) {
                    ps.setObject(i + 1, params[i++]);
                }
            }
            rs = ps.executeQuery();
            t = callback.handle(rs);
        }finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return t;
    }

    /**
     * Created by alleyz on 2017/4/7 0007.
     * 查询结果回调接口
     */
    public interface ResultSetCallback<T> {
        T handle(ResultSet rs) throws SQLException;
    }
}
