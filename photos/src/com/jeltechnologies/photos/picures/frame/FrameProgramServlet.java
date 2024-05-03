package com.jeltechnologies.photos.picures.frame;

import java.io.IOException;
import java.util.List;

import com.jeltechnologies.photos.Environment;
import com.jeltechnologies.photos.picures.frame.program.Group;
import com.jeltechnologies.photos.servlet.BaseServlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/frame/programs")
public class FrameProgramServlet extends BaseServlet {
    private static final long serialVersionUID = 8251873514791463457L;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	List<Group> groups = Environment.INSTANCE.getFrameProgramGroups();
	respondJson(response, groups);
    }

}
