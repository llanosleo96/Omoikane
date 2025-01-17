package omoikane.repository;

import omoikane.producto.Articulo;
import org.synyx.hades.dao.GenericDao;
import org.synyx.hades.dao.Query;
import org.synyx.hades.domain.Pageable;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: octavioruizcastillo
 * Date: 27/07/11
 * Time: 00:58
 * To change this template use File | Settings | File Templates.
 */
public interface ProductoRepo extends GenericDao<Articulo, Long>
{
    List<Articulo> findByCodigo(String codigo);

    @Query("FROM Articulo a JOIN FETCH a.baseParaPrecio bp WHERE a.descripcion like ?1")
    List<Articulo> findByDescripcionLike(String descripcion, Pageable pageable);

    @Query("FROM Articulo a JOIN FETCH a.stock s WHERE a.idArticulo = ?1")
    Articulo findByIdIncludeStock(Long id);

    @Query("SELECT cp.producto FROM CodigoProducto cp WHERE cp.codigo = ?1")
    List<Articulo> findByCodigoAlterno(String codigo);

    //Usar éste método en lugar de readAll para mayor eficiencia
    @Query("SELECT a FROM Articulo a JOIN FETCH a.baseParaPrecio")
    List<Articulo> findAll();

    @Query("SELECT a FROM Articulo a JOIN FETCH a.baseParaPrecio WHERE a.descripcion like ?1 OR a.codigo like ?1")
    List<Articulo> findByDescripcionLikeOrCodigoLike(String busqueda);

}
