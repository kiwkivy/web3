package com.help.web3.beans;

import com.google.gson.GsonBuilder;
import com.help.web3.util.Checker;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.validation.ValidationException;
import lombok.*;
import lombok.experimental.FieldDefaults;
import com.help.web3.entity.Result;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Named
@SessionScoped
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResultsBean implements Serializable {

    final EntityManagerFactory entityManagerFactory =
            Persistence.createEntityManagerFactory("connection");
    final List<Result> results = new CopyOnWriteArrayList<>();
    Result current = new Result();


    public void addResultFromGraph() {
        var params = FacesContext.getCurrentInstance()
                .getExternalContext().getRequestParameterMap();

        try {
            String xStr = params.get("x").replace(",", ".");
            double x = Double.parseDouble(xStr);
            double y = Double.parseDouble(params.get("y"));


            if (x >= -3 && x <= 3 && y >= -4 && y <= 4 ) {
                current.setX(String.valueOf(x));
                current.setY(String.valueOf(y));
                addResult();
            } else throw new ValidationException();
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Validation Error",
                    "Wrong parameters values."
            ));
        }
    }

    public void addResult() {
        current.setSuccessful(Checker.isOnGraph(
                Double.parseDouble(current.getX().replace(",", ".")),
                Double.parseDouble(current.getY()),
                Double.parseDouble(current.getR())));
        current.setTime(System.currentTimeMillis());
        results.add(current);
        initTransaction(manager -> manager.persist(current));
        current = current.clone();
    }

    public String parseResultsToJson() {

        return new GsonBuilder().create().toJson(results.stream()
                .peek(result -> result.setSuccessful(Checker.isOnGraph(
                                Double.parseDouble(result.getX().replace(",", ".")),
                                Double.parseDouble(result.getY()),
                                Double.parseDouble(current.getR())
                )))
                .toArray());
    }

    private void initTransaction(Consumer<EntityManager> transaction) {
        EntityManager manager = entityManagerFactory.createEntityManager();
        try {
            manager.getTransaction().begin();
            transaction.accept(manager);
            manager.getTransaction().commit();
        } catch (Exception ex) {
            if (manager.getTransaction().isActive()) {
                manager.getTransaction().rollback();
            }
            System.out.println("An exception occurred during transaction.");
            ex.printStackTrace();
        } finally {
            manager.close();
        }
    }

    public void validateX(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        String doubleStr = value.toString().replace(",", ".");
        try {
            double x = Double.parseDouble(doubleStr);
            if (x < -3 || x >= 3) {
                throw new ValidatorException(new FacesMessage("Value is invalid!"));
            }
        } catch (Exception e) {
            throw new ValidatorException(new FacesMessage("Value is invalid!"));
        }
    }
}
