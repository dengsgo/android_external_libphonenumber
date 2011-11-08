/*
 * Copyright (C) 2011 The Libphonenumber Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.i18n.phonenumbers;

import com.android.i18n.phonenumbers.Phonemetadata.PhoneNumberDesc;

import java.util.regex.Pattern;

/*
 * Utility for international short phone numbers, such as short codes and emergency numbers. Note
 * most commercial short numbers are not handled here, but by the PhoneNumberUtil.
 *
 * @author Shaopeng Jia
 */
public class ShortNumberUtil {

  private final PhoneNumberUtil phoneUtil;

  public ShortNumberUtil() {
    phoneUtil = PhoneNumberUtil.getInstance();
  }

  // @VisibleForTesting
  ShortNumberUtil(PhoneNumberUtil util) {
    phoneUtil = util;
  }

  /**
   * Returns true if the number might be used to connect to an emergency service in the given
   * region.
   *
   * This method takes into account cases where the number might contain formatting, or might have
   * additional digits appended (when it is okay to do that in the region specified).
   *
   * @param number  the phone number to test
   * @param regionCode  the region where the phone number is being dialed
   * @return  if the number might be used to connect to an emergency service in the given region.
   */
  public boolean connectsToEmergencyNumber(String number, String regionCode) {
    number = PhoneNumberUtil.extractPossibleNumber(number);
    if (PhoneNumberUtil.PLUS_CHARS_PATTERN.matcher(number).lookingAt()) {
      // Returns false if the number starts with a plus sign. We don't believe dialing the country
      // code before emergency numbers (e.g. +1911) works, but later, if that proves to work, we can
      // add additional logic here to handle it.
      return false;
    }
    String normalizedNumber = PhoneNumberUtil.normalizeDigitsOnly(number);
    PhoneNumberDesc emergencyNumberDesc = phoneUtil.getMetadataForRegion(regionCode).getEmergency();
    Pattern emergencyNumberPattern =
        Pattern.compile(emergencyNumberDesc.getNationalNumberPattern());
    if (regionCode.equals("BR")) {
      // This is to prevent Brazilian local numbers which start with 911 being incorrectly
      // classified as emergency numbers. In Brazil, it is impossible to append additional digits to
      // an emergency number to dial the number.
      if (!emergencyNumberPattern.matcher(normalizedNumber).matches()) {
        return false;
      }
    }

    // Check the prefix against possible emergency numbers for this region.
    return emergencyNumberPattern.matcher(normalizedNumber).lookingAt();
  }
}
