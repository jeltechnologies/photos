package com.jeltechnologies.photos.tags;

import com.jeltechnologies.icons.IconTag;
import com.jeltechnologies.photos.datatypes.NamedValue;
import com.jeltechnologies.photos.datatypes.NamedValueCollection;
import com.jeltechnologies.photos.utils.StringUtils;

public abstract class MenuTag extends BaseTag {
    private static final long serialVersionUID = -2933313346806858004L;
    
    private final static String MENU_ICON = new IconTag("three-dots-vertical").toString();

    private final static String ACTION_SPACES = StringUtils.dup(6, ' ');
    
    private String title;
    
    private String titleid;
    
    public abstract NamedValueCollection<String> getActions() throws Exception;
    
    @Override
    public void addHTML() throws Exception {
	addMenu();
    }
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getTitleid() {
        return titleid;
    }

    public void setTitleid(String titleid) {
        this.titleid = titleid;
    }

    private String addLink(String label, String javascriptFunction) {
	StringBuilder b = new StringBuilder();
	b.append("<a href=\"javascript:").append(javascriptFunction).append("\">");
	b.append(label);
	b.append("</a>");
	return b.toString();
    }
    
    private void addMenu() throws Exception {
	addLine("");
	
	String navigationID = getParameter("navigation_id");
	if (navigationID !=null && !navigationID.equals(navigationID)) {
	    navigationID = "-" + navigationID;
	} else {
	    navigationID = "TEST";
	}
	
	StringBuilder c = new StringBuilder("  <div ");
	if (id != null && !id.equals("")) {
	    c.append("id=\"").append(id).append(navigationID).append("\"" );
	}
	c.append(" class=\"dropdown");
	if (cssClass != null) {
	    c.append(" ").append(cssClass);
	}
	c.append("\">");
	addLine(c.toString());
	addLine("    <button onclick=\"toggleDropdown()\" class=\"dropbtn\">");
	add(MENU_ICON);

	addLine("    </button>");
	addLine("    <div id=\"myDropdown\" class=\"dropdown-content\">");
	NamedValueCollection<String> actions = getActions();
	for (NamedValue<String> action : actions) {
	    addLine(ACTION_SPACES + addLink(action.getName(), action.getValue()));
	}
	addLine("    </div>");
	addLine("  </div>");
    }
    
}
