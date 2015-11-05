package controllers

import play.api.mvc.{Action, Controller}

/**
  * Created by leo on 15-11-3.
  */
class Index extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.index())
  }

}
