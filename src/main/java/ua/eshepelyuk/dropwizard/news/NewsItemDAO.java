package ua.eshepelyuk.dropwizard.news;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

@RegisterMapper(NewsItemDAO.NewsItemMapper.class)
public interface NewsItemDAO {
    @SqlUpdate("insert into news_item (title, author, content, publishDate) values (:title, :author, :content, :publishDate)")
    @GetGeneratedKeys
    Long insert(@BindBean NewsItem item);

    @SqlQuery("select * from news_item")
    Collection<NewsItem> findAll();

    @SqlQuery("select * from news_item where id = :id")
    NewsItem findById(@Bind("id") Long id);

    class NewsItemMapper implements ResultSetMapper<NewsItem> {
        public NewsItem map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            return new NewsItem(
                    r.getLong("id"),
                    r.getString("title"),
                    r.getString("author"),
                    r.getString("content"),
                    r.getTimestamp("publishDate")
            );
        }
    }
}
