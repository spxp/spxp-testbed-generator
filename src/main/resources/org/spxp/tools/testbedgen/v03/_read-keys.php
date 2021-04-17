<?php

header('Content-Type: application/json');

if(isset($_GET['profile'])) {
  $json = json_decode(file_get_contents($_GET['profile']), true);
}

$readerKid = $_GET['reader'];

$datapool = array();

$requestQueue = array();
$processed = array();

array_push($requestQueue, $readerKid);

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
      $first_round = key($v2);
      $response[$k1][$k2][$first_round] = $v2[$first_round];
      $requiredDecryptionKey = get_required_decrypt_kid($v2[$first_round]);
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
  $kidParts = explode(".", $kid);
  $groupId = $kidParts[0];
  $roundId = $kidParts[1];
  foreach($data as $k1 => $v1) {
    if(isset($v1[$groupId][$roundId])) {
      return true;
    }
  }
  return false;
}

function add_key(&$source, &$target, $kid) {
  $kidParts = explode(".", $kid);
  $groupId = $kidParts[0];
  $roundId = $kidParts[1];
  foreach($source as $k1 => $v1) {
    if(isset($v1[$groupId][$roundId])) {
      $nextKey = $v1[$groupId][$roundId];
      $target[$k1][$groupId][$roundId] = $nextKey;
      return get_required_decrypt_kid($nextKey);
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