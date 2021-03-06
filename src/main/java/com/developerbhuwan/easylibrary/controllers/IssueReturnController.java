package com.developerbhuwan.easylibrary.controllers;

import com.developerbhuwan.easylibrary.entities.IssueReturn;
import com.developerbhuwan.easylibrary.controllers.util.JsfUtil;
import com.developerbhuwan.easylibrary.controllers.util.PaginationHelper;
import com.developerbhuwan.easylibrary.ejbs.BookIssueFacade;
import com.developerbhuwan.easylibrary.ejbs.IssueReturnFacade;
import com.developerbhuwan.easylibrary.entities.BookIssue;

import java.io.Serializable;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

@Named("issueReturnController")
@RequestScoped
public class IssueReturnController implements Serializable {

    @EJB
    private BookIssueFacade bookIssueFacade;
    private static final long serialVersionUID = 1L;

    private IssueReturn current;
    private DataModel items = null;
    @EJB
    private com.developerbhuwan.easylibrary.ejbs.IssueReturnFacade ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;

    public IssueReturnController() {
    }

    public IssueReturn getSelected() {
        if (current == null) {
            current = new IssueReturn();
            selectedItemIndex = -1;
        }
        return current;
    }

    private IssueReturnFacade getFacade() {
        return ejbFacade;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(10) {

                @Override
                public int getItemsCount() {
                    return getFacade().count();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareView() {
        current = (IssueReturn) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new IssueReturn();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            getFacade().create(current); 
            BookIssue bookIssue = current.getBookIssueId();
            bookIssueFacade.remove(bookIssue);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/ReturnFine").getString("IssueReturnCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/ReturnFine").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (IssueReturn) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/ReturnFine").getString("IssueReturnUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/ReturnFine").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (IssueReturn) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreatePagination();
        recreateModel();
        return "List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "List";
        }
    }

    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/ReturnFine").getString("IssueReturnDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/ReturnFine").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getFacade().count();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex + 1}).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    private void recreatePagination() {
        pagination = null;
    }

    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "List";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
    }

    public IssueReturn getIssueReturn(java.lang.String id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = IssueReturn.class)
    public static class IssueReturnControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            IssueReturnController controller = (IssueReturnController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "issueReturnController");
            return controller.getIssueReturn(getKey(value));
        }

        java.lang.String getKey(String value) {
            java.lang.String key;
            key = value;
            return key;
        }

        String getStringKey(java.lang.String value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof IssueReturn) {
                IssueReturn o = (IssueReturn) object;
                return getStringKey(o.getReturnId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + IssueReturn.class.getName());
            }
        }

    }

}
