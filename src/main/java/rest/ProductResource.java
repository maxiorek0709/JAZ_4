package rest;

import domain.Category;
import domain.Comment;
import domain.Product;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Path("/products")
@Stateless
public class ProductResource {
    @PersistenceContext
    private EntityManager em;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Product> getProducts() {
        return em.createNamedQuery("product.all", Product.class).getResultList();
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Product> searchProducts(@Context UriInfo uriInfo) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Product> criteriaQuery = criteriaBuilder.createQuery(Product.class);
        Root<Product> product = criteriaQuery.from(Product.class);
        criteriaQuery.select(product);
        List<Predicate> predicates = new LinkedList<>();
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        ParameterExpression<Double> priceFromParam = null;
        if (queryParameters.containsKey("priceFrom")) {
            priceFromParam = criteriaBuilder.parameter(Double.class);
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(product.get("price"), priceFromParam));
        }
        ParameterExpression<Double> priceToParam = null;
        if (queryParameters.containsKey("priceTo")) {
            priceToParam = criteriaBuilder.parameter(Double.class);
            predicates.add(criteriaBuilder.lessThanOrEqualTo(product.get("price"), priceToParam));
        }
        ParameterExpression<String> nameParam = null;
        if (queryParameters.containsKey("name")) {
            nameParam = criteriaBuilder.parameter(String.class);
            predicates.add(criteriaBuilder.like(product.get("name"), nameParam));
        }
        ParameterExpression<Category> categoryParam = null;
        if (queryParameters.containsKey("category")) {
            categoryParam = criteriaBuilder.parameter(Category.class);
            predicates.add(criteriaBuilder.equal(product.get("category"), categoryParam));
        }

        criteriaQuery.where(predicates.toArray(new Predicate[] {}));

        TypedQuery<Product> productTypedQuery = em.createQuery(criteriaQuery);

        if (queryParameters.containsKey("priceFrom")) {
            productTypedQuery.setParameter(priceFromParam, Double.valueOf(queryParameters.getFirst("priceFrom")));
        }
        if (queryParameters.containsKey("priceTo")) {
            productTypedQuery.setParameter(priceToParam, Double.valueOf(queryParameters.getFirst("priceTo")));
        }
        if (queryParameters.containsKey("name")) {
            productTypedQuery.setParameter(nameParam, "%" + queryParameters.getFirst("name") + "%");
        }
        if (queryParameters.containsKey("category")) {
            try {
                productTypedQuery.setParameter(categoryParam, Category.valueOf(queryParameters.getFirst("category")));
            } catch (IllegalArgumentException e) {
                return new ArrayList<>();
            }
        }

        return productTypedQuery.getResultList();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProduct(@PathParam("id") Integer id) {
        Product product;
        try {
            product = em.createNamedQuery("product.id", Product.class)
                    .setParameter("productId", id)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return Response.status(404).build();
        }
        return Response.ok(product).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Product addProduct(Product product) {
        em.persist(product);
        return product;
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProduct(@PathParam("id") Integer id, Product product) {
        try {
            em.createNamedQuery("product.id", Product.class)
                    .setParameter("productId", id)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return Response.status(404).build();
        }

        product.setId(id);
        em.merge(product);
        return Response.ok(product).build();
    }

    @GET
    @Path("/{id}/comments")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getComments(@PathParam("id") Integer id) {
        Product product;
        try {
            product = em.createNamedQuery("product.id", Product.class)
                    .setParameter("productId", id)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return Response.status(404).build();
        }
        return Response.ok(product.getComments()).build();
    }

    @POST
    @Path("/{id}/comments")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addComment(@PathParam("id") Integer id, Comment comment) {
        Product product;
        try {
            product = em.createNamedQuery("product.id", Product.class)
                    .setParameter("productId", id)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return Response.status(404).build();
        }
        product.getComments().add(comment);
        comment.setProduct(product);
        em.persist(comment);

        return Response.ok(comment).build();
    }

    @DELETE
    @Path("/{id}/comments/{commentId}")
    public Response deleteComment(@PathParam("id") Integer id, @PathParam("commentId") Integer commentId) {
        Product product;
        try {
            product = em.createNamedQuery("product.id", Product.class)
                    .setParameter("productId", id)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return Response.status(404).build();
        }
        List<Comment> comments = product.getComments();
        for (int i = 0; i < comments.size(); i++) {
            if (comments.get(i).getId().equals(commentId)) {
                comments.get(i).setProduct(null);
                comments.remove(i);
            }
        }
        em.merge(product);

        return Response.ok().build();
    }
}
