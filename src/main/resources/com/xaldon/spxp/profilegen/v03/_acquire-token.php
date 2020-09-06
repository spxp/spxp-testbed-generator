<?php

if(isset($_GET['return_scheme'])) {
  echo '<html><body>';
  echo '<a href="javascript:window.location.href=\''.htmlentities($_GET['return_scheme']).':WRdExl26rGAz701tMoiuJ\'">I am not a robot</a>';
  echo '</body></html>';
} else if(isset($_GET['return_uri'])) {
  echo '<html><body>';
  echo '<form action="'.htmlentities($_GET['return_uri']).'" method="post">';
  echo '<input type="hidden" id="token" name="token" value="WRdExl26rGAz701tMoiuJ">';
  echo '<button type="submit">I am not a robot</button>';
  echo '</form>';
  echo '</body></html>';
}

?>