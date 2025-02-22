package com.attendance.util;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class DBConnectionListener implements ServletContextListener {
    private static final Logger LOGGER = Logger.getLogger(DBConnectionListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent event) {
        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            event.getServletContext().setAttribute("DBConnection", conn);
            LOGGER.info("✅ Database connection initialized and stored in ServletContext!");
        } else {
            LOGGER.severe("❌ Failed to establish database connection!");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        Connection conn = (Connection) context.getAttribute("DBConnection");
        if (conn != null) {
            DBConnection.closeConnection(conn);
            LOGGER.info("✅ Database connection closed successfully!");
        }
    }
}
