package es.diadam.diadam.repositories;

import es.diadam.diadam.HelloApplication;
import es.diadam.diadam.managers.ManagerBBDD;
import es.diadam.diadam.models.Producto;
import es.diadam.diadam.services.Storage;
import es.diadam.diadam.utils.Properties;
import es.diadam.diadam.utils.Resources;
import es.diadam.diadam.utils.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class ProductoRepository implements IProductosRepository{
    private static ProductoRepository instance;
    private final ObservableList<Producto> repository = FXCollections.observableArrayList();
    private final Storage storage = Storage.getInstance();
    Logger logger = LogManager.getLogger(ProductoRepository.class);
   ManagerBBDD db = ManagerBBDD.getInstance();

    public static ProductoRepository getInstance() {
        if (instance == null) {
            instance = new ProductoRepository();
        }
        return instance;
    }


    private ProductoRepository() {

        initData();
    }


    private void initData() {
        if(repository.isEmpty()){
            logger.info("Inicializando datos");
            try{
                create(new Producto(UUID.randomUUID().toString(), "Pollo", 10, 2.50, "Carne", Resources.getPath(HelloApplication.class, "images/carne.png")));
                create(new Producto(UUID.randomUUID().toString(), "Merluza", 15, 2.00, "Pescado", Resources.getPath(HelloApplication.class, "images/pescado.png")));
                create(new Producto(UUID.randomUUID().toString(), "Ternera", 12, 2.30, "Carne", Resources.getPath(HelloApplication.class, "images/carne.png")));
                create(new Producto(UUID.randomUUID().toString(), "Pescadilla", 19, 1.50, "Pescado", Resources.getPath(HelloApplication.class, "images/pescado.png")));
                create(new Producto(UUID.randomUUID().toString(), "Solomillo", 9, 2.90, "Carne", Resources.getPath(HelloApplication.class, "images/carne.png")));
                create(new Producto(UUID.randomUUID().toString(), "Salmon", 17, 2.20, "Pescado", Resources.getPath(HelloApplication.class, "images/pescado.png")));
                create(new Producto(UUID.randomUUID().toString(), "Pavo", 10, 2.50, "Carne", Resources.getPath(HelloApplication.class, "images/carne.png")));
                create(new Producto(UUID.randomUUID().toString(), "Atun", 10, 3.00, "Pescado", Resources.getPath(HelloApplication.class, "images/pescado.png")));
            }catch(SQLException | IOException e ){
                logger.error("Error al inicializar datos");
            }
        }
    }


    @Override
    public  ObservableList<Producto>  findAll() throws SQLException {
        String sql = "select * from productos";
        db.open();
        ResultSet rs = db.select(sql).orElseThrow(() -> new SQLException("Error al obtener todos las productos"));
        repository.clear();
        while (rs.next()) {
            repository.add(
                    new Producto(
                            rs.getString("id"),
                            rs.getString("nombre"),
                            rs.getInt("stock"),
                            rs.getDouble("cantidad"),
                            rs.getString("avatar"),
                            rs.getString("descripcion")
                    )
            );
        }
    db.close();
        if(repository.isEmpty()){
            initData();
        }
        return repository;
    }



    @Override
    public Optional<Producto> findByDescripcion(String descripcion) throws SQLException {
        String query = "SELECT * FROM producto WHERE descripcion = ?";
        db.open();
        ResultSet result = db.select(query, descripcion).orElseThrow(() -> new SQLException("Error al consultar producto con descripcion " + descripcion));
        if (result.first()) {
            Producto producto = new Producto(
                    result.getString("id"),
                    result.getString("nombre"),
                    result.getInt("stock"),
                    result.getDouble("cantidad"),
                    result.getString("avatar"),
                    result.getString("descripcion")
            );
            db.close();
            return Optional.of(producto);
        }
        return Optional.empty();
    }



    @Override
    public Optional<Producto> create(Producto producto) throws SQLException, IOException {
        storeAvatar(producto);

        String sql = "INSERT INTO personas (id, nombre, stock, precio, descripcion, avatar) VALUES (?, ?, ?, ?, ?, ?)";
        db.open();
        ResultSet res= db.insert(sql, producto.getId(), producto.getNombre(), producto.getStock(), producto.getPrecio(), producto.getDescripcion(), producto.getAvatar())
                .orElseThrow(() -> new SQLException("Error al insertar pais"));
        if (res.first()) {
            producto.setId(res.getString(1));
        db.close();
        repository.add(producto);
        return Optional.of(producto);
    }
        return Optional.empty();
    }

    @Override
    public Optional<Producto> update(Producto producto) throws SQLException, IOException {
        int index = repository.indexOf(producto);
        storeAvatar(producto);
        String sql = "UPDATE personas SET nombre = ?, apellidos = ?, calle = ?, ciudad = ?, email = ?, cumpleaños = ?, avatar = ? WHERE id = ?";
        db.open();
        int res = db.update(sql, producto.getId(), producto.getNombre(), producto.getStock(), producto.getPrecio(), producto.getDescripcion(), producto.getAvatar());
        db.close();
        repository.set(index, producto);
        return Optional.of(producto);
    }

    @Override
    public Optional<Producto> delete(Producto producto) throws SQLException, IOException {
        deleteAvatar(producto);
        String sql = "DELETE FROM personas WHERE id = ?";
        db.open();
        db.delete(sql, producto.getId());
        db.close();
        repository.remove(producto);
        return Optional.of(producto);
    }

    @Override
    public void deleteAvatar(Producto producto) throws IOException {
        String source = Properties.IMAGES_DIR + File.separator + producto.getId() + "." + Utils.getFileExtension(producto.getAvatar()).orElse("png");
        storage.deleteFile(source);
    }

    @Override
    public void storeAvatar(Producto producto) throws IOException {
        String destination = Properties.IMAGES_DIR + File.separator + producto.getId() + "." + Utils.getFileExtension(producto.getAvatar()).orElse("png");
        String source = producto.getAvatar().replace("file:", "");
        logger.info("Origen: " + source);
        logger.info("Destino: " + destination);
        storage.copyFile(source, destination);
        producto.setAvatar(destination);
    }


}
