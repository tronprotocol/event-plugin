package org.tron.mongodb;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.bson.Document;
import org.bson.conversions.Bson;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.tron.mongodb.util.Converter;
import org.tron.mongodb.util.Pager;

public abstract class MongoTemplate {

    private MongoManager manager;
    private MongoCollection<Document> collection = null;

    public MongoTemplate(MongoManager manager) {
        this.manager = manager;
    }

    protected abstract String collectionName();

    protected abstract <T> Class<T> getReferencedClass();

    public void add(Document document) {
        MongoCollection<Document> collection = getCollection();
        collection.insertOne(document);
    }

    public void addEntity(String entity) {
        MongoCollection<Document> collection = getCollection();
        if (Objects.nonNull(collection)){
            collection.insertOne(Converter.jsonStringToDocument(entity));
        }
    }

    public void addEntityList(List<String> entities) {
        MongoCollection<Document> collection = getCollection();
        List<Document> documents = new ArrayList<Document>();
        if (entities != null && !entities.isEmpty()) {
            for (String entity : entities) {
                documents.add(Converter.jsonStringToDocument(Converter.objectToJsonString(entity)));
            }
        }
        collection.insertMany(documents);
    }

    public void addList(List<Document> documents) {
        MongoCollection<Document> collection = getCollection();
        collection.insertMany(documents);
    }

    public long update(String updateColumn, Object updateValue, String whereColumn, Object whereValue) {
        MongoCollection<Document> collection = getCollection();
        UpdateResult result = collection.updateMany(Filters.eq(whereColumn, whereValue),
                new Document("$set", new Document(updateColumn, updateValue)));
        return result.getModifiedCount();
    }

    public UpdateResult updateMany(Bson filter, Bson update) {
        MongoCollection<Document> collection = getCollection();
        UpdateResult result = collection.updateMany(filter, update);
        return result;
    }

    public long delete(String whereColumn, String whereValue) {
        MongoCollection<Document> collection = getCollection();
        DeleteResult result = collection.deleteOne(Filters.eq(whereColumn, whereValue));
        return result.getDeletedCount();
    }

    public DeleteResult deleteMany(Bson filter) {
        MongoCollection<Document> collection = getCollection();
        return collection.deleteMany(filter);
    }

    /**
     * replace the new document
     *
     * @param filter
     * @param replacement
     */
    public void replace(Bson filter, Document replacement) {
        MongoCollection<Document> collection = getCollection();
        collection.replaceOne(filter, replacement);
    }

    public List<Document> queryByCondition(Bson filter) {
        MongoCollection<Document> collection = getCollection();
        List<Document> documents = new ArrayList<Document>();
        FindIterable<Document> iterables = collection.find(filter);
        MongoCursor<Document> mongoCursor = iterables.iterator();
        while (mongoCursor.hasNext()) {
            documents.add(mongoCursor.next());
        }
        return documents;
    }

    public Document queryOne(String key, String value) {
        Bson filter = Filters.eq(key, value);
        return this.getCollection().find(filter).first();
    }

    public Document queryOneEntity(String key, String value) {
        Bson filter = Filters.eq(key, value);
        Document document = this.getCollection().find(filter).first();
        String jsonString = document.toJson();
        return Converter.jsonStringToObject(jsonString, this.getReferencedClass());
    }

    public List<Document> queryAll() {
        MongoCollection<Document> collection = getCollection();
        FindIterable<Document> findIterable = collection.find();
        MongoCursor<Document> mongoCursor = findIterable.iterator();
        List<Document> documents = new ArrayList<Document>();
        while (mongoCursor.hasNext()) {
            documents.add(mongoCursor.next());
        }
        return documents;
    }

    public <T> List<T> queryAllEntity() {
        MongoCollection<Document> collection = getCollection();
        FindIterable<Document> findIterable = collection.find();
        List<T> list = getEntityList(findIterable);
        return list;
    }

    public <T> Pager<T> queryPagerList(Bson filter, int pageIndex, int pageSize) {
        MongoCollection<Document> collection = getCollection();
        long totalCount = collection.count(filter);
        FindIterable<Document> findIterable = collection.find().skip((pageIndex - 1) * pageSize).sort(new BasicDBObject()).limit(pageSize);
        List<T>	resultList = getEntityList(findIterable);
        Pager<T> pager = new Pager<T>(resultList, totalCount, pageIndex, pageSize);
        return pager;
    }

    public MongoManager getManager() {
        return manager;
    }

    public void setManager(MongoManager manager) {
        this.manager = manager;
    }

    private <T> List<T> getEntityList(FindIterable<Document> findIterable) {
        MongoCursor<Document> mongoCursor = findIterable.iterator();
        List<T> list = new ArrayList<T>();
        Document document = null;
        while (mongoCursor.hasNext()) {
            document = mongoCursor.next();
            T object;
            try {
                object = Converter.jsonStringToObject(document.toJson(), getReferencedClass());
                list.add(object);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    private MongoCollection<Document> getCollection() {
        if (Objects.isNull(manager) || Objects.isNull(manager.getDb())){
            return null;
        }

        if (Objects.isNull(collection)) {
            collection = manager.getDb().getCollection(collectionName());
        }

        return collection;
    }

}
