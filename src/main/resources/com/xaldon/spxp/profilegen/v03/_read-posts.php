<?php

header('Content-Type: application/json');

if(isset($_GET['profile'])) {
  $json = json_decode(file_get_contents($_GET['profile']), true);
  $data = $json['data'];
}

$tzutc = new DateTimeZone('UTC');
$before = DateTime::createFromFormat('Y-m-d\TH:i:s.u', $_GET['before'], $tzutc);
$after = DateTime::createFromFormat('Y-m-d\TH:i:s.u', $_GET['after'], $tzutc);
$max = 50;
if(isset($_GET['max']) && is_numeric($_GET['max'])) {
  $max = intval($_GET['max']);
  if($max < 1)
    $max = 1;
  if($max > 100)
    $max = 100;
}

header('Content-Type: application/json');

$response = array(
  "data" => array(),
  "more" => false
);

$cnt = 0;
foreach($data as $elem) {
  $ts = DateTime::createFromFormat('Y-m-d\TH:i:s.u', $elem['seqts'], $tzutc);
  if($before && ($ts->getTimestamp() >= $before->getTimestamp()))
    continue;
  if($after && ($ts->getTimestamp() <= $after->getTimestamp()))
    continue;
  if($cnt >= $max) {
    $response['more'] = true;
    break;
  }
  $response['data'][] = $elem;
  $cnt++;
}

echo json_encode($response);

?>