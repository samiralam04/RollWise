package com.attendance.util;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Connection;

@WebListener
public class DBConnectionListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        Connection conn = DBConnection.getConnection();
        event.getServletContext().setAttribute("DBConnection", conn);
        System.out.println("✅ Database connection initialized and stored in ServletContext!");
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        Connection conn = (Connection) event.getServletContext().getAttribute("DBConnection");
        DBConnection.closeConnection(conn);
    }
}
