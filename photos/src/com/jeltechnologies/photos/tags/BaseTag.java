package com.jeltechnologies.photos.tags;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.icons.IconTag;
import com.jeltechnologies.photos.datatypes.usermodel.RoleModel;
import com.jeltechnologies.photos.datatypes.usermodel.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.SkipPageException;
import jakarta.servlet.jsp.tagext.TagSupport;

public abstract class BaseTag extends TagSupport {
    private static final long serialVersionUID = -5598986554561004141L;

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseTag.class);

    protected static final String BACK_BUTTON= 
	    "<span style=\"float: left\"><button onclick=\"window.history.back();\" class=\"backbtn\">" + new IconTag("back", 24).toString() + "</button></span>";

    protected HttpServletRequest request;
    
    protected HttpServletResponse response;

    protected String id;

    protected String cssClass;

    protected boolean addComment = true;

    protected User user;

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public String getCssClass() {
	return cssClass;
    }

    public void setCssClass(String cssClass) {
	this.cssClass = cssClass;
    }

    protected String getParameter(String name) {
	return request.getParameter(name);
    }

    protected void add(Object object) throws JspException {
	if (object != null) {
	    String line = object.toString();
	    try {
		pageContext.getOut().write(line);
	    } catch (Exception exception) {
		LOGGER.error("Error while creating html in tag", exception);
		throw new SkipPageException(exception.getMessage());
	    }
	}
    }

    protected void addLine(Object object) throws JspException {
	if (object != null) {
	    add(object.toString());
	    addLine();
	}
    }

    protected void addLine() throws JspException {
	add("\r\n");
    }

    protected void addComment(Object object) throws JspException {
	if (addComment && object != null) {
	    addLine("");
	    add("<!-- ");
	    add(object.toString());
	    addLine("-->");
	}
    }

    public final int doStartTag() throws JspException {
	try {
	    request = (HttpServletRequest) pageContext.getRequest();
	    response = (HttpServletResponse) pageContext.getResponse();
	    user = RoleModel.getUser(pageContext);
	    String className = this.getClass().getSimpleName();
	    if (addComment) {
		addComment(className + " start");
	    }
	    addHTML();
	    if (addComment) {
		addComment(className + " end");
	    }
	} catch (Exception e) {
	    throw new JspException(e);
	}
	return SKIP_BODY;
    }

    protected String addInputText(String inputId, String name, String value, boolean hidden, boolean multipleLines) {
	StringBuilder b = new StringBuilder();
	String displayValue;
	if (value == null) {
	    displayValue = "";
	} else {
	    displayValue = value;
	}

	b.append("<label for=\"").append(inputId).append("\">");
	b.append(name).append("</label>");

	if (multipleLines) {
	    b.append("<textarea id=\"").append(inputId);
	    b.append("\" name=\"").append(inputId).append("\" rows=\"20\" cols=\"40\">");
	    b.append(value);
	    b.append("</textarea>");

	} else {
	    b.append("<input type=\"text\" id=\"");
	    b.append(inputId).append("\" name=\"");
	    b.append(inputId).append("\" maxlength=\"255\" size=\"40\" value=\"");
	    b.append(displayValue).append("\">");
	}
	return b.toString();
    }

    public abstract void addHTML() throws Exception;

}
