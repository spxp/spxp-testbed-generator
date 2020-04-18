<?php

header('Content-Type: application/json');

if(isset($_GET['profile'])) {
  $json = json_decode(file_get_contents($_GET['profile']), true);
}

$connectionId = $_GET['connectionId'];

$datapool = array();

$requestQueue = array();
$processed = array();

array_push($requestQueue, $connectionId);

while($next = array_shift($requestQueue)) {
  array_push($processed, $next);
  if(isset($json[$next])) {
    $datapool[$next] = $json[$next];
    foreach($json[$next] as $key => $value) {
      if(!array_key_exists($key, $requestQueue) and !array_key_exists($key, $processed)) {
        array_push($requestQueue, $key);
      }
    }
  }
}

unset($json);

$response = array();
$requiredKeys = array();

if(isset($_GET['request'])) {
  if($_GET['request'] === 'ALL') {
    echo json_encode($datapool);
    return;
  }
  $requiredKeys = explode(",",$_GET['request']);
} else {
  foreach($datapool as $k1 => $v1) {
    foreach($v1 as $k2 => $v2) {
      reset($v2);
      $first_key = key($v2);
      $response[$k1][$k2][$first_key] = $v2[$first_key];
      $requiredDecryptionKey = get_required_decrypt_kid($v2[$first_key]);
      //$response[$k1][$k2]['___'] = $requiredDecryptionKey;
      array_push($requiredKeys, $requiredDecryptionKey);
    }
  }
}

while($requiredKey = array_shift($requiredKeys)) {
  if(!has_key($response, $requiredKey)) {
    $requires = add_key($datapool, $response, $requiredKey);
    if(isset($requires)) {
      if(!has_key($response, $requires) and !in_array($requires, $requiresKeys)) {
        array_push($requiredKeys, $requires);
      }
    }
  }
}

echo json_encode($response);

function has_key(&$data, $kid) {
  foreach($data as $k1 => $v1) {
    foreach($v1 as $k2 => $v2) {
      if(isset($v2[$kid])) {
        return true;
      }
    }
  }
  return false;
}

function add_key(&$source, &$target, $kid) {
  foreach($source as $k1 => $v1) {
    foreach($v1 as $k2 => $v2) {
      if(isset($v2[$kid])) {
        $target[$k1][$k2][$kid] = $v2[$kid];
        return get_required_decrypt_kid($v2[$kid]);
      }
    }
  }
  return NULL;
}

function get_required_decrypt_kid($encrypted) {
  $hdr_part = explode(".",$encrypted)[0];
  $decoded_hdr = base64url_decode($hdr_part);
  $hdr_json = json_decode($decoded_hdr, true);
  return $hdr_json["kid"];
}

function base64url_decode($data) {
  return base64_decode(strtr($data, '-_', '+/'));
}

?>