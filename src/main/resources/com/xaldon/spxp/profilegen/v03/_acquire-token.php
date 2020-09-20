<?php
if(!isset($_GET['profile'])) {
  http_response_code(405);
  exit('Missing profile parameter');
}
if(isset($_GET['return_scheme'])) {
  echo '<!DOCTYPE html>';
  echo '<html><body>';
  echo 'Connect to <b>'.htmlentities($_GET['profile']).'</b><br/>';
  echo '<a href="'.htmlentities($_GET['return_scheme']).':WRdExl26rGAz701tMoiuJ">I am not a robot</a>';
  echo '</body></html>';
} else if(isset($_GET['return_uri'])) {
  echo '<!DOCTYPE html>';
  echo '<html><body>';
  echo 'Connect to <b>'.htmlentities($_GET['profile']).'</b><br/>';
  echo '<form action="'.htmlentities($_GET['return_uri']).'" method="post">';
  echo '<input type="hidden" id="token" name="token" value="WRdExl26rGAz701tMoiuJ">';
  echo '<button type="submit">I am not a robot</button>';
  echo '</form>';
  echo '</body></html>';
} else {
  http_response_code(405);
  exit('Missing return_scheme or return_uri parameter');
}
?>