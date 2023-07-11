module org.klojang.db {

    exports org.klojang.jdbc;

    requires java.sql;

    requires org.apache.commons.text;
    requires org.apache.commons.lang3;
    requires org.apache.httpcomponents.httpclient;
    requires org.slf4j;

    requires org.klojang.check;
    requires org.klojang.util;
    requires org.klojang.collections;
    requires org.klojang.convert;
    requires org.klojang.invoke;
    requires org.klojang.templates;
}
